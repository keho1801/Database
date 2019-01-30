
package shoedb;

import java.util.ArrayList;
import java.util.List;

public class Customer {
String name;
int id;
String city;
List<Order> orders;


Customer(String name,int id, String city){
    orders = new ArrayList<>();
    this.name = name;
    this.id = id;
    this.city = city;    
}
}