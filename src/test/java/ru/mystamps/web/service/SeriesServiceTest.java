/*
 * Copyright (C) 2009-2012 Slava Semushin <slava.semushin@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package ru.mystamps.web.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.springframework.web.multipart.MultipartFile;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.fest.assertions.api.Assertions.assertThat;

import ru.mystamps.web.dao.CountryDao;
import ru.mystamps.web.dao.GibbonsCatalogDao;
import ru.mystamps.web.dao.MichelCatalogDao;
import ru.mystamps.web.dao.ScottCatalogDao;
import ru.mystamps.web.dao.SeriesDao;
import ru.mystamps.web.dao.YvertCatalogDao;
import ru.mystamps.web.entity.Country;
import ru.mystamps.web.entity.GibbonsCatalog;
import ru.mystamps.web.entity.MichelCatalog;
import ru.mystamps.web.entity.ScottCatalog;
import ru.mystamps.web.entity.Series;
import ru.mystamps.web.entity.User;
import ru.mystamps.web.entity.YvertCatalog;
import ru.mystamps.web.model.AddSeriesForm;
import ru.mystamps.web.tests.fest.DateAssert;

public class SeriesServiceTest {
	
	@Mock
	private CountryDao countryDao;
	
	@Mock
	private MichelCatalogDao michelCatalogDao;
	
	@Mock
	private ScottCatalogDao scottCatalogDao;
	
	@Mock
	private YvertCatalogDao yvertCatalogDao;
	
	@Mock
	private GibbonsCatalogDao gibbonsCatalogDao;
	
	@Mock
	private ImageService imageService;
	
	@Mock
	private SeriesDao seriesDao;
	
	@Mock
	private UserService userService;
	
	@Mock
	private MultipartFile multipartFile;
	
	@Captor
	private ArgumentCaptor<Series> seriesCaptor;
	
	@Captor
	private ArgumentCaptor<Set<MichelCatalog>> michelCatalogCaptor;
	
	@Captor
	private ArgumentCaptor<Set<ScottCatalog>> scottCatalogCaptor;
	
	@Captor
	private ArgumentCaptor<Set<YvertCatalog>> yvertCatalogCaptor;
	
	@Captor
	private ArgumentCaptor<Set<GibbonsCatalog>> gibbonsCatalogCaptor;
	
	@Captor
	private ArgumentCaptor<MultipartFile> multipartFileCaptor;
	
	@InjectMocks
	private SeriesService service = new SeriesService();
	
	private AddSeriesForm form;
	
	@BeforeMethod
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		form = new AddSeriesForm();
		form.setQuantity(2);
		form.setPerforated(false);
		
		when(imageService.save(any(MultipartFile.class))).thenReturn("/fake/path/to/image");
		when(userService.getCurrentUser()).thenReturn(UserServiceTest.getValidUser());
	}
	
	//
	// Tests for add()
	//
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void addShouldThrowExceptionArgumentIsNull() {
		service.add(null);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void addShouldThrowExceptionIfQuantityIsNull() {
		form.setQuantity(null);
		
		service.add(form);
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void addShouldThrowExceptionIfPerforatedIsNull() {
		form.setPerforated(null);
		
		service.add(form);
	}
	
	@Test
	public void addShouldPassEntityToSeriesDao() {
		service.add(form);

		verify(seriesDao).save(any(Series.class));
	}
	
	@Test
	public void addShouldLoadAndPassCountryToSeriesDaoIfCountryPresent() {
		final Country expectedCountry = CountryServiceTest.getCountry();
		final Integer expectedId      = expectedCountry.getId();
		final String expectedName     = expectedCountry.getName();
		
		form.setCountry(expectedId);
		
		when(countryDao.findOne(anyInt())).thenReturn(expectedCountry);
		
		service.add(form);
		
		verify(countryDao).findOne(eq(expectedId));
		verify(seriesDao).save(seriesCaptor.capture());

		assertThat(seriesCaptor.getValue().getCountry()).isNotNull();
		assertThat(seriesCaptor.getValue().getCountry().getId()).isEqualTo(expectedId);
		assertThat(seriesCaptor.getValue().getCountry().getName()).isEqualTo(expectedName);
	}
	
	@Test
	public void addShouldPassDateWithSpecifiedYearToSeriesDaoIfYearPresent() {
		final int expectedYear = 2000;
		form.setYear(expectedYear);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(seriesCaptor.getValue().getReleasedAt());
		final int actualYear = cal.get(Calendar.YEAR);
		
		assertThat(actualYear).isEqualTo(expectedYear);
	}
	
	@Test
	public void addShouldPassQuantityToSeriesDao() {
		final Integer expectedQuantity = 3;
		form.setQuantity(expectedQuantity);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getQuantity()).isEqualTo(expectedQuantity);
	}
	
	@Test
	public void addShouldPassPerforatedToSeriesDao() {
		final Boolean expectedResult = true;
		form.setPerforated(expectedResult);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getPerforated()).isEqualTo(expectedResult);
	}

	@Test
	public void addShouldPassNullToSeriesDaoIfMichelNumbersIsNull() {
		assertThat(form.getMichelNumbers()).isNull();
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getMichel()).isNull();
	}
	
	@Test
	public void addShouldSaveMichelNumbers() {
		final Set<MichelCatalog> expectedNumbers = ImmutableSet.of(
			new MichelCatalog("1"),
			new MichelCatalog("2")
		);
		form.setMichelNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(michelCatalogDao).save(michelCatalogCaptor.capture());
		assertThat(michelCatalogCaptor.getValue()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassMichelNumbersToSeriesDao() {
		final Set<MichelCatalog> expectedNumbers = ImmutableSet.of(
			new MichelCatalog("1"),
			new MichelCatalog("2")
		);
		form.setMichelNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getMichel()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassNullToSeriesDaoIfScottNumbersIsNull() {
		assertThat(form.getScottNumbers()).isNull();
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getScott()).isNull();
	}
	
	@Test
	public void addShouldSaveScottNumbers() {
		final Set<ScottCatalog> expectedNumbers = ImmutableSet.of(
			new ScottCatalog("1"),
			new ScottCatalog("2")
		);
		form.setScottNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(scottCatalogDao).save(scottCatalogCaptor.capture());
		assertThat(scottCatalogCaptor.getValue()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassScottNumbersToSeriesDao() {
		final Set<ScottCatalog> expectedNumbers = ImmutableSet.of(
			new ScottCatalog("1"),
			new ScottCatalog("2")
		);
		form.setScottNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getScott()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassNullToSeriesDaoIfYvertNumbersIsNull() {
		assertThat(form.getYvertNumbers()).isNull();
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getYvert()).isNull();
	}
	
	@Test
	public void addShouldSaveYvertNumbers() {
		final Set<YvertCatalog> expectedNumbers = ImmutableSet.of(
			new YvertCatalog("1"),
			new YvertCatalog("2")
		);
		form.setYvertNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(yvertCatalogDao).save(yvertCatalogCaptor.capture());
		assertThat(yvertCatalogCaptor.getValue()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassYvertNumbersToSeriesDao() {
		final Set<YvertCatalog> expectedNumbers = ImmutableSet.of(
			new YvertCatalog("1"),
			new YvertCatalog("2")
		);
		form.setYvertNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getYvert()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassNullToSeriesDaoIfGibbonsNumbersIsNull() {
		assertThat(form.getGibbonsNumbers()).isNull();
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getGibbons()).isNull();
	}
	
	@Test
	public void addShouldSaveGibbonsNumbers() {
		final Set<GibbonsCatalog> expectedNumbers = ImmutableSet.of(
			new GibbonsCatalog("1"),
			new GibbonsCatalog("2")
		);
		form.setGibbonsNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(gibbonsCatalogDao).save(gibbonsCatalogCaptor.capture());
		assertThat(gibbonsCatalogCaptor.getValue()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassGibbonsNumbersToSeriesDao() {
		final Set<GibbonsCatalog> expectedNumbers = ImmutableSet.of(
			new GibbonsCatalog("1"),
			new GibbonsCatalog("2")
		);
		form.setGibbonsNumbers(Joiner.on(",").join(expectedNumbers));
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getGibbons()).isEqualTo(expectedNumbers);
	}
	
	@Test
	public void addShouldPassImageToImageService() {
		form.setImage(multipartFile);
		service.add(form);
		
		verify(imageService).save(multipartFileCaptor.capture());
		assertThat(multipartFileCaptor.getValue()).isEqualTo(multipartFile);
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void addShouldThrowExceptionIfImageUrlIsNull() {
		when(imageService.save(any(MultipartFile.class))).thenReturn(null);
		
		service.add(form);
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void addShouldThrowExceptionIfImageUrlTooLong() {
		final String aVeryLongPath = StringUtils.repeat("x", Series.IMAGE_URL_LENGTH + 1);
		when(imageService.save(any(MultipartFile.class))).thenReturn(aVeryLongPath);
		
		service.add(form);
	}
	
	@Test
	public void addShouldPassImageUrlToSeriesDao() {
		final String expectedUrl = "http://example.org/example.jpg";
		when(imageService.save(any(MultipartFile.class))).thenReturn(expectedUrl);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getImageUrl()).isEqualTo(expectedUrl);
		
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void addShouldThrowExceptionIfCommentIsEmpty() {
		form.setComment("  ");

		service.add(form);
	}
	
	@Test
	public void addShouldPassCommentToSeriesDaoIfPresent() {
		final String expectedComment = "Some text";
		form.setComment(expectedComment);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		assertThat(seriesCaptor.getValue().getComment()).isEqualTo(expectedComment);
	}
	
	@Test
	public void addShouldAssignCreatedAtToCurrentDate() {
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		DateAssert.assertThat(seriesCaptor.getValue().getCreatedAt()).isCurrentDate();
	}
	
	@Test
	public void addShouldAssignUpdatedAtToCurrentDate() {
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		
		DateAssert.assertThat(seriesCaptor.getValue().getUpdatedAt()).isCurrentDate();
	}
	
	@Test(expectedExceptions = IllegalStateException.class)
	public void addShouldThrowExceptionWhenCannotDetermineCurrentUser() {
		when(userService.getCurrentUser()).thenReturn(null);
		
		service.add(form);
	}
	
	@Test
	public void addShouldAssignCreatedAtToCurrentUser() {
		final User expectedUser = UserServiceTest.getValidUser();
		when(userService.getCurrentUser()).thenReturn(expectedUser);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getCreatedBy()).isEqualTo(expectedUser);
	}
	
	@Test
	public void addShouldAssignUpdatedAtToCurrentUser() {
		final User expectedUser = UserServiceTest.getValidUser();
		when(userService.getCurrentUser()).thenReturn(expectedUser);
		
		service.add(form);
		
		verify(seriesDao).save(seriesCaptor.capture());
		assertThat(seriesCaptor.getValue().getUpdatedBy()).isEqualTo(expectedUser);
	}
	
	//
	// Tests for findById()
	//
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void findByIdShouldThrowExceptionWhenIdIsNull() {
		service.findById(null);
	}
	
	@Test
	public void findByIdShouldCallSeriesDao() {
		final Integer anyId = 3;
		service.findById(anyId);
		
		verify(seriesDao).findOne(anyInt());
	}
	
	@Test
	public void findByIdShouldPassIdToSeriesDao() {
		final Integer expectedId = 3;
		
		service.findById(expectedId);
		
		verify(seriesDao).findOne(eq(expectedId));
	}
	
	@Test
	public void findByIdShouldReturnValueFromSeriesDao() {
		final Series expectedSeries = getSeries();
		final Integer seriesId = expectedSeries.getId();
		when(seriesDao.findOne(eq(seriesId))).thenReturn(expectedSeries);
		
		final Series actualSeries = service.findById(seriesId);
		
		assertThat(actualSeries).isEqualTo(expectedSeries);
	}
	
	private static Series getSeries() {
		final Series series = new Series();
		series.setId(1);
		return series;
	}
	
}