package at.technikum.application.model;

public record CardRec(String username, String name, Double damage, String cardType, String elementType) {
    public boolean monster() {
        return this.cardType.equals("monster");
    }
    public boolean spell() {
        return this.cardType.equals("spell");
    }


    public String USER() {
        return this.username.toUpperCase();
    }
}
