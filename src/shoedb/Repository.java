/*
 */

package shoedb;


import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author kenny
 */
public class Repository {

    String password = "lasniq123456";
    private Connection con;

    public Repository() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public Order getOrderByMakesorderId(int makesorderid) throws SQLException {

        List<Shoe> shoesInOrder = new ArrayList<>();
        List<Order> orders = new ArrayList<>();
        Order order = new Order();


        ResultSet rs = null;


        String query = "select  shoe.name, shoe.color, shoe.size, shoe.price, brand.name as brandname, shoe.id from orders \n" +
                " inner join adds on orders.adds_id = adds.id\n" +
                " inner join shoe on adds.shoe_id = shoe.id\n" +
                " inner join brand on shoe.brand_id = brand.id\n" +
                "where makesorder_id = ?";


        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        PreparedStatement stmt = con.prepareStatement(query);


        stmt.setString(1, makesorderid + "");


        rs = stmt.executeQuery();

        while (rs.next()) {

            shoesInOrder.add(new Shoe(rs.getString("name"), rs.getString("color")
                    , rs.getInt("size"), rs.getInt("price"), rs.getString("brandname"), rs.getInt("id")));

            order = new Order(shoesInOrder);


        }


        return order;
    }


    public List<Customer> getAllCustomers() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String query = "select customer.name, customer.id, city.name as cityname from customer inner join city on city_id = city.id";


        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        PreparedStatement stmt = con.prepareStatement(query);

        ResultSet rs = stmt.executeQuery(query);
        int i = 0;
        while (rs.next()) {

            customers.add(new Customer(rs.getString("name"), rs.getInt("id"), rs.getString("cityname")));
            customers.get(i).orders = assignList(customers.get(i).id);
            i++;

        }

        return customers;
    }

    public List<Order> assignList(int customerid) throws SQLException {

        ResultSet rs = null;

        List<Order> orders = new ArrayList<>();


        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        String query = "select makesorder_id, count(makesorder_id) as times, delivered from orders\n" +
                "inner join makesorder on makesorder_id = makesorder.id\n" +
                "where customer_id = ? group by makesorder_id";
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setString(1, customerid + "");

        rs = stmt.executeQuery();


        while (rs.next()) {
            List<Boolean> delivered = new ArrayList<>();
            boolean b;
            int makesorderid = rs.getInt("makesorder_id");
            int times = rs.getInt("times");
            int i = rs.getInt("delivered");

            if (times > 0) {
                Order o = new Order();

                if (i != 0) {
                    b = true;

                    delivered.add(b);
                } else if (i == 0) {
                    b = false;
                    delivered.add(b);
                }

                o = (getOrderByMakesorderId(makesorderid));
                o.delivered = delivered;

                orders.add(o);

            }

        }
        return orders;
    }

    public List<Shoe> getShoesFromStock() throws SQLException {

        List<Shoe> shoesFromStock = new ArrayList<>();

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        String query = "select shoe.name, shoe.color, shoe.size, shoe.price, brand.name as brandname, shoe.id, instock from adds "
                + "inner join shoe on adds.shoe_id = shoe.id "
                + "inner join brand on shoe.brand_id = brand.id "
                + "where instock != 0";

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {


            shoesFromStock.add(new Shoe(rs.getString("name"), rs.getString("color")
                    , rs.getInt("size"), rs.getInt("price"), rs.getString("brandname"), rs.getInt("id")));
            int stock = rs.getInt("instock");

        }
        // vi vill ta skor från adds och ifall stock == 0 så kommer inte listan ha den skon.
        return shoesFromStock;
    }

    public void setDelivered(int customerid) throws SQLException {

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        String query = "select * from makesorder\n" +
                "where customer_id =" + customerid;

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {

            rs.updateBoolean("delivered", true);

            rs.updateRow();

        }
    }

    public int addToCart(int customerid, int shoeid) throws SQLException {

        List<Shoe> shoesFromStock = new ArrayList<>();

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);

        String query = "select * from makesorder\n" +
                " where customer_id =" + customerid + " and delivered=0";

        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        ResultSet rs = stmt.executeQuery(query);

        int makesorderid;

        if (rs.next()) {
            makesorderid = rs.getInt("id");
        } else {
            makesorderid = 0;
        }


        CallableStatement stm = con.prepareCall("{CALL AddToCart(?,?,?)}");
        stm.setInt(1, makesorderid);
        stm.setInt(2, shoeid);
        stm.setInt(3, customerid);
        int returnValue = stm.executeUpdate();
        System.out.println(returnValue);
        

        stm.close();
        con.close();


        shoesFromStock = getShoesFromStock();
//
//        for(shoedatabase.Shoe s: shoesFromStock){
//            System.out.println(s.name);
//        }
        return returnValue;
    }

    public int gettingIdForCustomer(String customerName) throws SQLException {
        ResultSet rs = null;
        List<Customer> customers = getAllCustomers();

        int customerid = 0;
        for (Customer c : customers) {
            if (c.name.equalsIgnoreCase(customerName)) {
                customerid = c.id;
            }
        }
        String query = "select count(*) as totalorders from makesorder\n" +
                "where customer_id = ? and delivered = 0";

        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/shoedb", "root", password);
        PreparedStatement stmt = con.prepareStatement(query);

        stmt.setString(1, customerid + "");

        rs = stmt.executeQuery();

        while (rs.next()) {
            int count = rs.getInt("totalorders");
            System.out.println(customerName + " you have " + count + " undelivered orders");
        }
        return customerid;

    }
    public void forUser() {
        List<Shoe> shoes = null;
        List<Customer> customers = null;
        try {
            shoes = getShoesFromStock();
            Scanner userInput = new Scanner(System.in);
            
            System.out.println("Here are all the customers");
            customers = getAllCustomers();
            for(Customer c: customers){
                System.out.println(c.name);
            } 
            System.out.println("Write customer name");
            String firstName = userInput.nextLine();
            int customerid = gettingIdForCustomer(firstName);
            
            System.out.println("Do you want to add shoe to order Y/N");
            String answer = userInput.nextLine();
            
            if(answer.equalsIgnoreCase("y")){
                System.out.println("This is all the shoes from stock");
                for(Shoe s : shoes){
                    System.out.println(s.name);
                }
                System.out.println("Which shoe do you want to add?");
                String shoeName = userInput.nextLine();
                for(Shoe s : shoes){
                    if(shoeName.equalsIgnoreCase(s.name)){
                        int shoeId = s.id;
                        int returnedValue = addToCart(customerid, shoeId);
                            if(returnedValue > 0){
                                System.out.println("You've successfully added the shoe");
                            }else{
                                System.out.println("You couldnt add shoes to the order");
                            }

                    }
                }

            }else if(answer.equalsIgnoreCase("n")){
                System.out.println("goodbye");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }



    }
}