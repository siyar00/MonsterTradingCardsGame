package at.technikum.application.service;

import at.technikum.application.model.Card;
import at.technikum.application.repository.packages.PackagesRepository;

import java.util.List;

public record PackageService(PackagesRepository packagesRepository) {
    public String createPackages(List<Card> cardList){
        return packagesRepository.createPackages(cardList);
    }
    public String acquirePackages(String username) {
        return packagesRepository.acquirePackages(username);
    }
}
