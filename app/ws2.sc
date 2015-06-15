
object ws2 {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val board = "...-..=.-";                        //> board  : String = ...-..=.-
  val b = board.grouped(3).zipWithIndex.flatMap {
    case(w,r) => w.zipWithIndex.flatMap {
      case ('-',c) => Some((r,c,false))
      case _ => None
    }
  }.toList                                        //> b  : List[(Int, Int, Boolean)] = List((1,0,false), (2,2,false))
  
  val vec = Vector(Vector(1,2),Vector(3,4))       //> vec  : scala.collection.immutable.Vector[scala.collection.immutable.Vector[I
                                                  //| nt]] = Vector(Vector(1, 2), Vector(3, 4))
  val v = vec.flatMap(x=>{x}).mkString            //> v  : String = 1234
  
}