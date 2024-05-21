package dk.kea.bilabonnement.model;

public class Bruger {
    int medarbejderId;
    String login;
    String password;
    String role;


    public Bruger(String login, String password, String type){
        this.login = login;
        this.password = password;
        this.role = type;
    }
    public Bruger(){
    }

    public String getUsername(){
        return login;
    }
    public String getPassword(){
        return password;
    }
    public String getRole(){return role;}

    public void setUsername(String username){
        this.login = username;
    }
    public void setRole(String type){
        this.role = type;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void setMedarbejderId(int medarbejderId){
        this.medarbejderId = medarbejderId;
    }
}