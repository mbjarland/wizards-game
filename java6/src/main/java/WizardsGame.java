import java.util.EnumMap;
import java.util.Map;

import static WizardsGame.Location.*;
import static WizardsGame.Direction.*;
import static WizardsGame.Portal.*;
import static WizardsGame.Thing.*;
import static WizardsGame.Command.*;

public class WizardsGame {
	enum Location   { livingRoom, garden, attic }
	enum Direction  { west, upstairs, east, downstairs }
	enum Portal     { door, ladder }
  enum Thing      { whiskey, bucket, chain, frog }
  enum Command    { look, walk, pickup, inventory }

  private static class Edge {
    Location to;
		Direction d;
		Portal p;

		Edge(Location to, Direction d, Portal p) {
      this.to = to;
			this.d = d;
			this.p = p;
		}
	}

	private static Map<Location, String>  nodes           = new EnumMap<Location, String>(Location.class);
	private static Map<Location, Edge[]>  edges           = new EnumMap<Location, Edge[]>(Location.class);
  private static Map<Location, Thing[]> objectLocations = new EnumMap<Location, Thing[]>(Location.class);

  private Location location = livingRoom;

	static {
		nodes.put(livingRoom, "You are in the living room.\nA wizard is snoring loudly on the couch\n");
    nodes.put(garden,     "You are in a beautiful garden.\nThere is a well in front of you\n");
    nodes.put(attic,      "You are in the attic.\nThere is a giant welding torch in the corner\n");

    edges.put(livingRoom, new Edge[] { new Edge(garden, west, door), new Edge(attic, upstairs, ladder) });
    edges.put(garden,     new Edge[] { new Edge(livingRoom, east, door)});
    edges.put(attic,      new Edge[] { new Edge(livingRoom, downstairs, ladder)});

    objectLocations.put(livingRoom, new Thing[]{whiskey, bucket});
    objectLocations.put(garden, new Thing[]{chain, frog});
  }


	public static void main(String[] args) {
    new WizardsGame().gameRepl();
  }




	private String describeLocation(location, nodes) {
    return nodes[location]
  }

	private String describePath(edge) {
    "There is a ${edge[1]} going ${edge[0]} from here."
  }

	def describePaths(location, edges) { 
		edges[location].collect { k, v -> describePath(v) }.join('\n') + '\n' 
	}

	def describeObjects(location, objectLocations) {
		objectLocations[location].collect { obj -> "You see a $obj on the floor" }.join('\n')
	}

	def look() {
		describeLocation(location, nodes) +
		describePaths(location, edges) + 
		describeObjects(location, objectLocations)
	}

	def walk(direction) {
		def next = edges[location].find { loc, dirs -> direction == dirs[0] }?.key
		if (next) {
			location = next
			look()
		} else "You cannot go that way."
	}

	def pickup(object) {
		if (object in objectLocations[location]) {
			objectLocations[location] -= object
			objectLocations.body      += object
			"You are now carrying the $object"
		} else {
			"You cannot get that."
		}
	}

	def inventory() { "items- ${objectLocations.body.join(', ')}" }

	def gameRepl() {
		println "\n${look()}\n" 
		while ((cmd = gameRead()) != 'quit') { println gameEval(cmd) + "\n" } 
	}

	def gameEval(cmd) {
		def t = cmd.tokenize(" ")
		if (!(t.head() in allowedCommands)) return "I do not know that command!"
			
		cmd = t.head() + (t.size() == 1 ? "() " : " ") + t.tail().collect { "'$it'" }.join(" ")

		Eval.me 'g', this, "g.$cmd"
	}

	def gameRead() { System.console().readLine("~> ") }
}