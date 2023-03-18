package at.technikum.application.service;

import at.technikum.application.repository.UsersRepository;

public class GameService {

    private final UsersRepository usersRepository;

    public GameService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }
}
