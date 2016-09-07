package osgi.enroute.examples.jdbc.addressbook.dao.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import osgi.enroute.examples.jdbc.addressbook.dao.datatypes.AddressDTO;

@Entity
@Table(name="person_addresses")
public class Address {

	private String emailAddress;
	
	private Person person;
	
	private String city;
	
	private String country;
	
	@Id
	@Column(name="email_address", nullable=false, length=255)
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="person_id", nullable=false)
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@Column(name="city", length=100, nullable=false)
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}

	@Column(name="country", length=2, nullable=false)
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	@Transient
	public AddressDTO toDTO() {
		AddressDTO dto = new AddressDTO();
		dto.city = city;
		dto.country = country;
		dto.emailAddress = emailAddress;
		dto.personId = getPerson().getPersonId();
		return dto;
	}
	
	public static Address fromDTO(AddressDTO dto) {
		
		Address address = new Address();
		address.setCity(dto.city);
		address.setCountry(dto.country);
		address.setEmailAddress(dto.emailAddress);
		address.setPerson(new Person());
		address.getPerson().setPersonId(dto.personId);
		
		return address;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((emailAddress == null) ? 0 : emailAddress.hashCode());
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
		Address other = (Address) obj;
		if (emailAddress == null) {
				return false;
		} else if (!emailAddress.equals(other.emailAddress))
			return false;
		return true;
	}
	
}
