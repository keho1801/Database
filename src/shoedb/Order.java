package shoedb;

import java.util.ArrayList;
import java.util.List;



public class Order {
List <Shoe> shoes = new ArrayList<>();
List<Boolean> delivered;

Order(List<Shoe> shoes){
   delivered = new ArrayList<>();
   this.shoes = shoes;

}
Order(){
    
}

}
