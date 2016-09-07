package osgi.enroute.examples.jdbc.addressbook.dao.provider;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osgi.enroute.examples.jdbc.addressbook.dao.api.AddressDao;
import osgi.enroute.examples.jdbc.addressbook.dao.datatypes.AddressDTO;
import osgi.enroute.examples.jdbc.addressbook.dao.entities.Address;
import osgi.enroute.examples.jdbc.addressbook.dao.entities.Person;

@Component(name = "osgi.enroute.examples.jdbc.addressbook.personaddress.dao", service = AddressDao.class, configurationPid = "osgi.enroute.examples.jdbc.addressbook.dao")
public class AddressDaoImpl implements AddressDao {

	private Logger logger = LoggerFactory.getLogger(AddressDaoImpl.class);

	@Reference
	TransactionControl transactionControl;

	@Reference(name="provider")
	JPAEntityManagerProvider jpaEntityManagerProvider;

	EntityManager em;

	@Activate
	void activate(Map<String, Object> props) throws SQLException {
		em = jpaEntityManagerProvider.getResource(transactionControl);
	}

	@Override
	public List<AddressDTO> select(Long personId) {

		List<AddressDTO> personsAddresses = transactionControl.notSupported(() -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Address> query = cb.createQuery(Address.class);
			
			return em.createQuery(query).getResultList().stream()
				.map(Address::toDTO)
				.collect(toList());
		});

		return personsAddresses;
	}

	@Override
	public AddressDTO findByPK(String pk) {

		AddressDTO personAddress = transactionControl.supports(() -> {

			return ofNullable(em.find(Address.class, pk))
					.map(Address::toDTO)
					.orElse(null);
		});

		return personAddress;
	}

	@Override
	public void save(Long personId, AddressDTO data) {

		transactionControl.required(() -> {
			Person p = em.find(Person.class, personId);
			
			if(p != null) {
				Address fromDTO = Address.fromDTO(data);
				fromDTO.setPerson(p);
				em.persist(fromDTO);
			} else {
				throw new IllegalArgumentException("No person exists with id " + personId);
			}
			
			return null;
		});
	}

	@Override
	public void update(Long personId, AddressDTO data) {

		transactionControl.required(() -> {
			
			Address a = em.find(Address.class, data.emailAddress);
			
			if(a != null) {
				a.setCity(data.city);
				a.setCountry(data.country);
				if(!a.getPerson().getPersonId().equals(data.personId)) {
					a.setPerson(em.find(Person.class, data.personId));
				}
			} else {
				throw new IllegalArgumentException("No address exists for id: " + data.emailAddress);
			}

			return null;

		});
	}

	@Override
	public void delete(Long personId) {

		transactionControl.required(() -> {
			
			Person p = em.find(Person.class, personId);
			
			if(p != null) {
				p.getAddresses().clear();
				logger.info("Deleted Person {} Addresses", personId);
			}
			
			return null;
		});
	}

}
