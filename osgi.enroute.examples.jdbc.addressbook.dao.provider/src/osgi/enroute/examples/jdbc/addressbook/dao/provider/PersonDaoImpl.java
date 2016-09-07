package osgi.enroute.examples.jdbc.addressbook.dao.provider;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

import osgi.enroute.examples.jdbc.addressbook.dao.api.PersonDao;
import osgi.enroute.examples.jdbc.addressbook.dao.datatypes.AddressDTO;
import osgi.enroute.examples.jdbc.addressbook.dao.datatypes.PersonDTO;
import osgi.enroute.examples.jdbc.addressbook.dao.entities.Address;
import osgi.enroute.examples.jdbc.addressbook.dao.entities.Person;

/**
 * 
 */
@Component(name = "osgi.enroute.examples.jdbc.addressbook.person.dao", service = PersonDao.class, configurationPid = "osgi.enroute.examples.jdbc.addressbook.dao")
public class PersonDaoImpl implements PersonDao {

	private Logger logger = LoggerFactory.getLogger(PersonDaoImpl.class);

	@Reference
	TransactionControl transactionControl;

	@Reference(name="provider")
	JPAEntityManagerProvider jpaEntityManagerProvider;

	EntityManager em;

	@Activate
	void start(Map<String, Object> props) throws SQLException {
		try {
			em = jpaEntityManagerProvider.getResource(transactionControl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<PersonDTO> select() {

		List<PersonDTO> persons = transactionControl.notSupported(() -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Person> query = cb.createQuery(Person.class);
			query.from(Person.class);
			
			return em.createQuery(query).getResultList().stream()
				.map(Person::toDTO)
				.collect(toList());
		});

		return persons;
	}

	@Override
	public void delete(Long primaryKey) {

		transactionControl.required(() -> {
			
			Person person = em.find(Person.class, primaryKey);
			if(person != null) {
				em.remove(person);
				logger.info("Deleted Person with ID : {}", primaryKey);
			} else {
				logger.warn("There was no Person with ID : {} to delete");
			}
			return null;
		});
	}

	@Override
	public PersonDTO findByPK(Long pk) {

		PersonDTO person = transactionControl.supports(() -> {
			return ofNullable(em.find(Person.class, pk))
				.map(Person::toDTO)
				.orElse(null);
		});

		return person;
	}

	@Override
	public Long save(PersonDTO data) {

		long personPK = transactionControl.required(() -> {

			Person p = Person.fromDTO(data);
			p.setPersonId(null);
			em.persist(p);
			em.flush();
			
			logger.info("Saved Person with ID : {}", p.getPersonId());

			return p.getPersonId();
		});

		return personPK;
	}

	@Override
	public void update(PersonDTO data) {

		transactionControl.required(() -> {

			Map<String, AddressDTO> addresses = data.addresses.stream()
					.collect(Collectors.toMap(a -> a.emailAddress, Function.identity()));

			List<Address> list = em.createQuery("Select a from Address a where a.person.personId = :id", Address.class)
				.setParameter("id", data.personId)
				.getResultList();
			
			list.forEach(a -> {
				AddressDTO dto = addresses.remove(a.getEmailAddress());
				if(dto != null) {
					a.setCity(dto.city);
					a.setCountry(dto.country);
				} else {
					em.remove(a);
				}
			});
			em.flush();
			
			Person p = em.find(Person.class, data.personId);
			
			if(p != null) {
				p.setFirstName(data.firstName);
				p.setLastName(data.lastName);
				
				addresses.values().forEach(dto -> {
					Address a = Address.fromDTO(dto);
					a.setPerson(p);
					p.getAddresses().add(a);
					em.persist(a);
				});
			}
			
			logger.info("Updated person : {}", data);

			return null;
		});
	}
}
