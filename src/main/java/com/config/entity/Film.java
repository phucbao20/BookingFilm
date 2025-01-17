package com.config.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.beans.factory.annotation.Autowired;

import com.config.service.FilmService;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Film" )
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
	
	@Id
	@Column(name = "FilmId")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	
	int filmId;
	@Column(name = "FilmName" , columnDefinition ="nvarchar(255)", nullable = false)
	String filmName;
	
	@Column(name = "FilmTime" , columnDefinition ="nvarchar(255)", nullable = false)
	String filmTime; 
	
	@Column(name = "FilmImage" , columnDefinition ="nvarchar(255)", nullable = false)
	String filmImage;

	@Column(name = "VideoTrailer" , columnDefinition ="nvarchar(255)", nullable = false)

	String videoTrailer;
	
	@Temporal(value = TemporalType.DATE)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "PremiereDate" , columnDefinition ="date", nullable = false)
	Date premiereDate;
	
	@Column(name = "Status", nullable = false)
	boolean status;
	
	@Column(name = "Price" , nullable = false)
	double price;
	
	@Column(name = "Age")
	int age;
	
	@Column(name = "Rate")
	double rate;
	
	@ManyToOne()
	@JoinColumn(name = "CountryId" , nullable = false)
	Country country;
	
	@OneToMany(mappedBy= "film" , cascade = CascadeType.ALL)
	List<FilmDetail> listFilmDetail;
	
	@OneToMany(mappedBy= "film" , cascade = CascadeType.ALL)
	List<ShowTime> listShowTime;
	
	@OneToMany(mappedBy= "film" , cascade = CascadeType.ALL)
	List<FilmGenres> listFilmGenres;
	
	
	
}
