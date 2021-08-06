package db;

public class Users {

    private String Login;
    private String Pass;

    public Users(String login, String pass) {
        Login = login;
        Pass = pass;
    }

    public String getLogin() {
        return Login;
    }

    public String getPass() {
        return Pass;
    }

    @Override
    public String toString() {
        return "Users{" +
                "Login='" + Login + '\'' +
                ", Pass='" + Pass + '\'' +
                '}';
    }
}
