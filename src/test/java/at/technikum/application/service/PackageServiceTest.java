package at.technikum.application.service;

import at.technikum.application.model.Card;
import at.technikum.application.repository.packages.PackagesRepository;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.when;

public class PackageServiceTest {

    @Mock
    PackagesRepository packagesRepository;

    @InjectMocks
    PackageService packageService;

    @BeforeTest
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createPackagesTest() {
        //Arrange
        when(packagesRepository.createPackages(Collections.singletonList(Card.builder().build()))).thenReturn("test");
        //Act
        String result = packageService.createPackages(Collections.singletonList(Card.builder().build()));
        //Assert
        Assertions.assertEquals("test", result);
    }

    @Test
    public void acquirePackagesTest() {
        //Arrange
        when(packagesRepository.acquirePackages("username")).thenReturn("test");
        //Act
        String result = packageService.acquirePackages("username");
        //Assert
        Assertions.assertEquals("test", result);
    }
}
