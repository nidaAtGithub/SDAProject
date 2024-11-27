package UserPackage;

import java.sql.SQLException;

abstract class User
{
    private String name;
    private String username;
    private String password;
    private static String cnic;
    private int age;
    private String phone;
    private String address;

    public User(String name, String username, String password, String cnic, int age, String phone,String address) //For candidate and voter
    {
        this.name = name;
        this.username = username;
        this.password = password;
        this.cnic = cnic;
        this.age = age;
        this.phone = phone;
        this.address = address;
    }
    public User(String name, String username, String password, String cnic,String phone, String address) //For Admin
    {
        this.name = name;
        this.username = username;
        this.password = password;
        this.cnic = cnic;
        this.phone = phone;
        this.address = address;
    }

    public User(String password, String username)  //admin login
    {
        this.username  =username;
        this.password = password;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public void setUsername(String username)
    {
        this.username = username;
    }
    public void setCnic(String cnic)
    {
        this.cnic = cnic;
    }
    public void setPassword(String password)
    {
        this.password  = password;
    }
    public void setAge(int age)
    {
        this.age  = age;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }
    public void setPhone(String phone)
    {
        this.phone = phone;
    }
    public String getName()
    {
        return name;
    }
    public String getUsername()
    {
        return username;
    }
    public static String getCnic()
    {
        return cnic;
    }
    public String getPassword()
    {
        return password;
    }
    public int getAge()
    {
        return age;
    }
    public String getPhone()
    {
        return phone;
    }
    public String getAddress()
    {
        return address;
    }

    public abstract void register();
    public abstract void login() throws SQLException;
}

