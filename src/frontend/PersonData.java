package frontend;

// Simple holder for person info before creating a member
public class PersonData {
    public Integer personId; // generated Person ID
    public String name;
    public String email;
    public String address;
    public String phone;

    public PersonData() {}
    public PersonData(String name, String email, String address, String phone) {
        this.name = name; this.email = email; this.address = address; this.phone = phone;
    }
}
