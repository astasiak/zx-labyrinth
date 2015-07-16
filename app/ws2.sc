
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
  
  
  val set = Set(1,2,3)--List(1,4)                 //> set  : scala.collection.immutable.Set[Int] = Set(2, 3)
  import game._
  var map: scala.collection.mutable.Map[PlayerId, Int] = scala.collection.mutable.Map()
                                                  //> map  : scala.collection.mutable.Map[game.PlayerId,Int] = Map()
  map.put(PlayerA,1)                              //> res0: Option[Int] = None
  map.get(PlayerA)                                //> res1: Option[Int] = Some(1)
  val s = map.keys.toSet                          //> s  : scala.collection.immutable.Set[game.PlayerId] = Set(PlayerA)
  val w: Set[PlayerId] = Set(PlayerA)             //> w  : Set[game.PlayerId] = Set(PlayerA)
  val ss: Set[PlayerId] = Set(PlayerA,PlayerB)    //> ss  : Set[game.PlayerId] = Set(PlayerA, PlayerB)
  val ss2 = Set(PlayerA,PlayerB)                  //> ss2  : scala.collection.immutable.Set[Product with Serializable with game.Pl
                                                  //| ayerId{def theOther: Product with Serializable with game.PlayerId}] = Set(Pl
                                                  //| ayerA, PlayerB)
  val sss = ss--w                                 //> sss  : scala.collection.immutable.Set[game.PlayerId] = Set(PlayerB)
  
  val str = "ABC|CBA|AAA"                         //> str  : String = ABC|CBA|AAA
  
  str.split('|')                                  //> res2: Array[String] = Array(ABC, CBA, AAA)
  str.split('|').map { row=>
    row.map (_ match {
      case 'A' => 1
      case 'B' => 2
      case 'C' => 3
    })
  }                                               //> res3: Array[scala.collection.immutable.IndexedSeq[Int]] = Array(Vector(1, 2,
                                                  //|  3), Vector(3, 2, 1), Vector(1, 1, 1))
}