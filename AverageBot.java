import java.util.*;
import java.util.function.Predicate;
public class AverageBot {
	private static boolean showPath = false;
	private static int totalLengthOfPaths = 0;
	private final CaveNavigator nav;
	private final List<Door> path = new ArrayList<>();
	private final List<Room> roomPath = new ArrayList<>();
	private final int lastMazeSteps;
	private final Map<Room, List<DE>> graph = new HashMap<>();
	private final Map<Room, Map<Door, Integer>> edgeCount = new HashMap<>();
	private final Set<Room> frontier = new HashSet<>();
	private final Set<Room> deadEnds = new HashSet<>();
	private final Deque<Door> planned = new ArrayDeque<>();
	private Room endRoom = null;
	private int endId = -1;
	private static class DE {
		private final Door door;
		private final Room nb;
		public DE(Door door, Room nb) { this.door = door; this.nb = nb; }
		public Door door() { return door; }
		public Room nb() { return nb; }
	}
	public AverageBot(String fileName) {
		SerialLoader loader = new SerialLoader();
		loader.deserialize("CaveData\\" + fileName + ".ser");
		nav = new CaveNavigator(loader);
		roomPath.add(nav.getCurrentRoom());
		frontier.add(nav.getCurrentRoom());
		lastMazeSteps = Room.getNumMoves();
	}
	public List<Door> getPath() { return path; }
	public static int getPaths(){
		return totalLengthOfPaths;
	}
	public void run() {
		while (!nav.atEnd()) {
			Room before = nav.getCurrentRoom();
			nav.move(chosen = pickSmartDoor());
			Room after = nav.getCurrentRoom();
			if (nav.atEnd()) { endRoom = after; endId = after.getID(); }
			addEdge(before, chosen, after); addEdge(after, chosen, before);
			path.add(chosen); roomPath.add(after);
			edgeCount.computeIfAbsent(before, k -> new HashMap<>()).merge(chosen, 1, Integer::sum);
			updateFrontier(before); updateFrontier(after);
			if (graph.getOrDefault(after, Collections.<DE>emptyList()).size() == 1) deadEnds.add(after); else deadEnds.remove(after);
		}
		optimizePath();
		report();
	}
	private Door chosen;
	//adds to graph map
	private void addEdge(Room from, Door d, Room to) {
		List<DE> es = graph.computeIfAbsent(from, k -> new ArrayList<>());
		if (es.stream().noneMatch(e -> e.door() == d)) es.add(new DE(d, to));
	}
	//maintains list of unexplored rooms only
	private void updateFrontier(Room r) {
		Map<Door, Integer> used = edgeCount.getOrDefault(r, Collections.<Door, Integer>emptyMap());
		if (graph.getOrDefault(r, Collections.<DE>emptyList()).stream().anyMatch(e -> used.getOrDefault(e.door(), 0) == 0)) frontier.add(r);
		else frontier.remove(r);
	}
	//next room lookup
	private Room nb(Room r, Door d) {
		return graph.getOrDefault(r, Collections.<DE>emptyList()).stream().filter(e -> e.door() == d).map(new java.util.function.Function<DE, Room>() { public Room apply(DE e) { return e.nb(); } }).findFirst().orElse(null);
	}
	private int h(Room r) { return endId < 0 ? 0 : Math.abs(r.getID() - endId); }
	//prioritization scoring by ID proximity
	//gotta choose moves that are best to go through next
	private List<Door> aStar(Room start, Predicate<Room> goal) {
		Map<Room, DE> from = new HashMap<>();
		Map<Room, Integer> g = new HashMap<>();
		PriorityQueue<Room> open = new PriorityQueue<>(Comparator.comparingInt(r -> g.getOrDefault(r, Integer.MAX_VALUE) + h(r)));
		g.put(start, 0); open.add(start); from.put(start, null);
		while (!open.isEmpty()) {
			Room cur = open.poll();
			if (goal.test(cur) && !cur.equals(start)) return reconstruct(start, cur, from);
			for (DE e : graph.getOrDefault(cur, Collections.<DE>emptyList())) {
				int ng = g.getOrDefault(cur, Integer.MAX_VALUE) + 1;
				if (ng < g.getOrDefault(e.nb(), Integer.MAX_VALUE)) { g.put(e.nb(), ng); from.put(e.nb(), new DE(e.door(), cur)); open.add(e.nb()); }
			}
		}
		return null;
	}
	//known algo for guaranteed shortest path by distance
	//quenue makes this so much easier to handle
	private List<Door> bfs(Room start, Predicate<Room> goal) {
		Map<Room, DE> from = new HashMap<>();
		Queue<Room> q = new ArrayDeque<>();
		q.add(start); from.put(start, null);
		while (!q.isEmpty()) {
			Room cur = q.poll();
			for (DE e : graph.getOrDefault(cur, Collections.<DE>emptyList())) {
				if (from.containsKey(e.nb())) continue;
				from.put(e.nb(), new DE(e.door(), cur));
				if (goal.test(e.nb())) return reconstruct(start, e.nb(), from);
				q.add(e.nb());
			}
		}
		return null;
	}
	//traceback for correct sequence
	private List<Door> reconstruct(Room start, Room end, Map<Room, DE> from) {
		LinkedList<Door> doors = new LinkedList<>();
		for (Room c = end; !c.equals(start); ) { DE s = from.get(c); doors.addFirst(s.door()); c = s.nb(); }
		return new ArrayList<>(doors);
	}
	//pick by priority then fallback to unexplored
	private Door pickSmartDoor() {
		if (!planned.isEmpty()) return planned.poll();
		Room cur = nav.getCurrentRoom();
		if (endRoom != null) {
			Room ef = endRoom;
			List<Door> r = bfs(cur, r2 -> r2.equals(ef));
			if (r != null && !r.isEmpty()) { r.subList(1, r.size()).forEach(planned::add); return r.get(0); }
		}
		Door d = pickUnexplored(cur);
		if (d != null) return d;
		d = routeToFrontier(cur);
		if (d != null) return d;
		List<Door> all = new ArrayList<>(nav.getDoors());
		return all.get((int) (Math.random() * all.size()));
	}
	//prefers unused door from current room
	private Door pickUnexplored(Room cur) {
		Map<Door, Integer> used = edgeCount.getOrDefault(cur, Collections.<Door, Integer>emptyMap());
		List<Door> unused = new java.util.ArrayList<>(); for (Door door : nav.getDoors()) if (used.getOrDefault(door, 0) == 0) unused.add(door);
		if (unused.isEmpty()) return null;
		List<Door> pool = new java.util.ArrayList<>(); for (Door door : unused) if (nb(cur, door) == null) pool.add(door);
		if (pool.isEmpty()) pool = unused;
		List<Door> cands = new java.util.ArrayList<>(); for (Door door : pool) if (!deadEnds.contains(nb(cur, door))) cands.add(door);
		if (cands.isEmpty()) cands = pool;
		if (endId >= 0) return cands.stream().min(Comparator.comparingInt(d -> { Room n = nb(cur, d); return n == null ? 0 : h(n); })).orElse(cands.get(0));
		return cands.get((int) (Math.random() * cands.size()));
	}
	//navigates to highest a* score room then passes route into planned
	private Door routeToFrontier(Room cur) {
		Room best = frontier.stream().filter(r -> !r.equals(cur)).max(Comparator.comparingInt(this::fScore)).orElse(null);
		if (best == null) return null;
		Room bf = best;
		List<Door> route = aStar(cur, r -> r.equals(bf));
		if (route == null || route.isEmpty()) route = bfs(cur, r -> frontier.contains(r) && !r.equals(cur));
		if (route == null || route.isEmpty()) return null;
		route.subList(1, route.size()).forEach(planned::add);
		return route.get(0);
	}
	//unexplored room scoring by id proximity and penalized dead ends
	private int fScore(Room r) {
		if (deadEnds.contains(r)) return -1000;
		Map<Door, Integer> used = edgeCount.getOrDefault(r, Map.of());
		int unex = (int) graph.getOrDefault(r, Collections.<DE>emptyList()).stream().filter(e -> used.getOrDefault(e.door(), 0) == 0).count();
		return unex * 10 + (endId >= 0 ? endId - Math.abs(r.getID() - endId) : 0);
	}
	//path segment improvements with breadth-first search (bfs) then implements shortcut check
	private void optimizePath() {
		removeLoops();
		boolean improved = true;
		while (improved) {
			improved = false;
			outer:
			for (int i = 0; i < roomPath.size() - 2; i++)
				for (int j = roomPath.size() - 1; j > i + 1; j--) {
					Room t = roomPath.get(j);
					List<Door> sc = bfs(roomPath.get(i), r -> r.equals(t));
					if (sc != null && sc.size() < j - i) { splice(i, j, sc); improved = true; break outer; }
				}
			removeLoops();
		}
		for (int i = 0; i < roomPath.size() - 1; i++)
			for (int j = roomPath.size() - 1; j > i + 1; j--) {
				Room t = roomPath.get(j);
				List<Door> sc = bfs(roomPath.get(i), r -> r.equals(t));
				if (sc != null && sc.size() < j - i) { splice(i, j, sc); removeLoops(); break; }
			}
	}
	private void splice(int i, int j, List<Door> sc) {
		path.subList(i, j).clear(); roomPath.subList(i + 1, j + 1).clear();
		path.addAll(i, sc);
		Room c = roomPath.get(i); List<Room> rms = new ArrayList<>();
		for (Door d : sc) { c = nb(c, d); rms.add(c); }
		roomPath.addAll(i + 1, rms);
	}
	//dawg do NOT REPEAT A PAth
	private void removeLoops() {
		boolean changed = true;
		while (changed) {
			changed = false;
			Map<Room, Integer> last = new HashMap<>();
			for (int i = 0; i < roomPath.size(); i++) last.put(roomPath.get(i), i);
			for (int i = 0; i < roomPath.size(); i++) {
				int l = last.get(roomPath.get(i));
				if (l > i) { path.subList(i, l).clear(); roomPath.subList(i + 1, l + 1).clear(); changed = true; break; }
			}
		}
	}
	//basic report for the path
	private void report() {
		totalLengthOfPaths += path.size();
		System.out.println("Found the End!\nsteps = " + (Room.getNumMoves() - lastMazeSteps) + " | path length = " + path.size());
		if (showPath) { System.out.print("\nPATH: START -> "); for (Door d : path) System.out.print(d + " -> "); System.out.println("END"); }
		System.out.println("__________________________________________________________________\n");
	}
	//main doesnt matter
	public static void main(String[] args) {
		for (String f : new String[]{"M1","M2","M3","M4","M5","M6","M7","M8","M9"}) new AverageBot(f).run();
		System.out.println("Done!\nTotal moves between Rooms = " + Room.getNumMoves());
		System.out.println("Total length of all paths = " + totalLengthOfPaths);
		System.out.println("Total used to Explore      = " + (Room.getNumMoves() - totalLengthOfPaths));
	}
}