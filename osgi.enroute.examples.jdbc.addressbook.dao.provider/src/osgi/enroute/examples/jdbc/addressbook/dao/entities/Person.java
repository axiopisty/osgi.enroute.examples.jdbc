package osgi.enroute.examples.jdbc.addressbook.dao.entities;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import osgi.enroute.examples.jdbc.addressbook.dao.datatypes.PersonDTO;

@Entity
@Table(name="persons")
public class Person {
	
	private Long personId;
	
	private String firstName;
	
	private String lastName;
	
	private List<Address> addresses;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="person_id", nullable=false)
	public Long getPersonId() {
		return personId;
	}

	public void setPersonId(Long personId) {
		this.personId = personId;
	}

	@Column(name="first_name", nullable=false, length=255)
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(name="last_name", nullable=false, length=255)
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@OneToMany(fetch=FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="person_id")
	public List<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<Address> addresses) {
		this.addresses = addresses;
	}

	@Transient
	public PersonDTO toDTO() {
		PersonDTO dto = new PersonDTO();
		dto.firstName = firstName;
		dto.lastName = lastName;
		dto.personId = personId;
		dto.addresses = getAddresses().stream()
					.map(Address::toDTO)
					.collect(toList());
		return dto;
	}
	
	public static Person fromDTO(PersonDTO dto) {
		Person person = new Person();
		person.setPersonId(dto.personId);
		person.setFirstName(dto.firstName);
		person.setLastName(dto.lastName);
		person.setAddresses(dto.addresses.stream()
					.map(Address::fromDTO)
					.map(a -> {
						a.setPerson(person);
						return a;
					})
					.collect(toList()));
		return person;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((personId == null) ? 0 : personId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Person other = (Person) obj;
		if (personId == null) {
				return false;
		} else if (!personId.equals(other.personId))
			return false;
		return true;
	}
	
	
}