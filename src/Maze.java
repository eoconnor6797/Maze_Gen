import java.util.*;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// comparator for edge weights
class WeightComp implements Comparator<Edge> {
    public int compare(Edge e1, Edge e2) {
        return e1.weight - e2.weight;
    }
}

//Class representing the player
class Player {
    Cell loc;
    //Constructor
    Player(Cell loc) {
        this.loc = loc;
    }
    //Draws the player
    void draw(WorldScene world, int cellSize) {
        world.placeImageXY(new CircleImage(cellSize / 2 - 1, OutlineMode.SOLID, Color.RED),
                this.loc.x * cellSize + cellSize / 2,
                this.loc.y * cellSize + cellSize / 2);
    }
}

// class for cells
class Cell {
    int x;
    int y;
    ArrayList<Cell> connection;
    //Constructor
    Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.connection = new ArrayList<Cell>();
    }
    // method to override .equals
    public boolean equals(Object o) {
        if (!(o instanceof Cell)) {
            return false;
        }
        else {
            return this.hashCode() == o.hashCode();
        }
    }
    // overriding the hashcode
    public int hashCode() {
        return this.x * 10000 + this.y;
    }
    //Draws the cell on the given world
    void draw(WorldScene world, int cellSize, Color c) {
        world.placeImageXY(new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, c),
                cellSize * this.x + cellSize / 2, cellSize * this.y + cellSize / 2);
    }
}

// class for edges
class Edge {
    Cell start;
    Cell end;
    int weight;
    //Constructor for edge
    Edge(Cell start, Cell end, int weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }
}

// class for the maze
class MazeWorld extends World {
    int x;
    int y;
    int cellSize;
    Player me;
    ArrayList<Edge> walls;
    ArrayList<Edge> span;
    ArrayList<Cell> cells;
    ArrayList<Cell> path;
    ArrayList<Cell> drawPath;
    ArrayList<Cell> finalPath;
    ArrayList<Cell> playerPath;
    boolean showPath;
    boolean showFinal;
    //Constructor
    MazeWorld(int x, int y, int cellSize) {
        this.x = x;
        this.y = y;
        this.cellSize = cellSize;
        this.span = new ArrayList<Edge>();
        this.cells = new ArrayList<Cell>();
        this.walls = new ArrayList<Edge>();
        this.path = new ArrayList<Cell>();
        this.drawPath = new ArrayList<Cell>();
        this.finalPath = new ArrayList<Cell>();
        this.playerPath = new ArrayList<Cell>();
        this.showPath = true;
        this.showFinal = true;
        this.createBoard(this.x, this.y, this.cellSize);
    }
    //Generates the initial board
    void createBoard(int x, int y, int cellSize) {
        Random rand = new Random();
        ArrayList<Edge> all = new ArrayList<Edge>();
        //Create cells based on inputs
        for (int i = 0; i < y * x; i += 1) {
            cells.add(new Cell(i % x, i / x));
        }
        //Create horizontal edges
        for (int i = 0; i < x * y; i += 1) {
            if (i % x - (x - 1) != 0) {
                all.add(new Edge(cells.get(i), cells.get(i + 1), rand.nextInt(20)));
            }
        }
        //Create vertical edges
        for (int i = 0; i < (y - 1) * x; i += 1) {
            all.add(new Edge(cells.get(i), cells.get(i + x), rand.nextInt(20)));
        }
        //Generates the minimum tree
        this.kruskal(all);
        //generates list of edges that a cell is a part of
        for (Edge e : this.span) {
            e.start.connection.add(e.end);
            e.end.connection.add(e.start);
        }
        //starts the player at top left cell
        this.me = new Player(this.cells.get(0));
    }
    // method to draw the world
    public WorldScene makeScene() {
        WorldScene world = getEmptyScene();
        //Draws player visited squares
        for (Cell c : this.playerPath) {
            c.draw(world, this.cellSize, Color.LIGHT_GRAY);
        }
        //Draws searched cells
        if (this.showPath) {
            for (Cell c : this.drawPath) {
                c.draw(world, this.cellSize, Color.BLUE);
            }
        }
        //Draws final path after search is done
        if (this.finalPath.size() > 0 && this.path.isEmpty() && this.showFinal) {
            for (Cell c : this.finalPath) {
                c.draw(world, this.cellSize, Color.CYAN);
            }
        }
        //Draw end square
        this.cells.get(this.x * this.y - 1).draw(world, this.cellSize, Color.PINK);
        //Draw start squre
        this.cells.get(0).draw(world, this.cellSize, Color.GREEN);
        //Draw walls
        for (Edge e : this.walls) {
            //Creates line
            LineImage wall = new LineImage(new Posn((e.end.y - e.start.y) * this.cellSize,
                    (e.end.x - e.start.x) * this.cellSize), Color.BLACK);
            //Places line
            world.placeImageXY(wall, e.end.x * this.cellSize +
                            (this.cellSize / 2) * (e.end.y - e.start.y),
                    e.end.y * this.cellSize + (this.cellSize / 2) * (e.end.x - e.start.x));
        }
        //Draws the player
        this.me.draw(world, this.cellSize);
        return world;
    }
    //Does on key event
    public void onKeyEvent(String key) {
        if (key.equals("left")) {
            for (Cell c : this.me.loc.connection) {
                if (this.me.loc.x - c.x > 0) {
                    this.me.loc = c;
                    this.playerPath.add(this.me.loc);
                }
            }
        }
        else if (key.equals("right")) {
            for (Cell c : this.me.loc.connection) {
                if (c.x - this.me.loc.x > 0) {
                    this.me.loc = c;
                    this.playerPath.add(this.me.loc);
                }
            }
        }
        else if (key.equals("up")) {
            for (Cell c : this.me.loc.connection) {
                if (this.me.loc.y - c.y > 0) {
                    this.me.loc = c;
                    this.playerPath.add(this.me.loc);
                }
            }
        }
        else if (key.equals("down")) {
            for (Cell c : this.me.loc.connection) {
                if (c.y - this.me.loc.y > 0) {
                    this.me.loc = c;
                    this.playerPath.add(this.me.loc);
                }
            }
        }
        else if (key.equals("f")) {
            this.drawPath.addAll(this.path);
            this.path = new ArrayList<Cell>();
        }
        else if (key.equals("h")) {
            if (this.showPath) {
                this.showPath = false;
            }
            else {
                this.showPath = true;
            }
        }
        else if (key.equals("H")) {
            if (this.showFinal) {
                this.showFinal = false;
            }
            else {
                this.showFinal = true;
            }
        }
        //Does dfs
        else if (key.equals("d")) {
            this.path = new ArrayList<Cell>();
            this.drawPath = new ArrayList<Cell>();
            this.finalPath = new ArrayList<Cell>();
            this.showPath = true;
            this.showFinal = true;
            this.search("d");
        }
        //Does bfs
        else if (key.equals("b")) {
            this.path = new ArrayList<Cell>();
            this.drawPath = new ArrayList<Cell>();
            this.finalPath = new ArrayList<Cell>();
            this.showPath = true;
            this.showFinal = true;
            this.search("b");
        }
        //Restarts the world
        else if (key.equals("r")) {
            this.span = new ArrayList<Edge>();
            this.cells = new ArrayList<Cell>();
            this.walls = new ArrayList<Edge>();
            this.drawPath = new ArrayList<Cell>();
            this.path = new ArrayList<Cell>();
            this.finalPath = new ArrayList<Cell>();
            this.playerPath = new ArrayList<Cell>();
            this.showPath = true;
            this.showFinal = true;
            this.createBoard(this.x, this.y, this.cellSize);
        }
    }
    //Ends the world
    public WorldEnd worldEnds() {
        //If player is on last cell
        if (this.me.loc.equals(this.cells.get(this.x * this.y - 1))) {
            WorldScene end = this.makeScene();
            end.placeImageXY(new TextImage("Good job", 32, Color.BLACK), this.cellSize * this.x / 2,
                    this.cellSize * this.y / 2);
            return new WorldEnd(true, end);
        }
        else {
            return new WorldEnd(false, this.makeScene());
        }
    }
    //Does something on tick
    public void onTick() {
        if (!(this.path.isEmpty())) {
            Cell c = this.path.get(0);
            this.path.remove(c);
            this.drawPath.add(c);
        }
    }
    //Searches based on bfs or dfs
    void search(String algo) {
        //Init
        ArrayList<Cell> worklist = new ArrayList<Cell>();
        HashMap<Cell, Cell> cameFromEdge = new HashMap<Cell, Cell>();
        worklist.add(this.cells.get(0));
        //Search
        while (worklist.size() > 0) {
            Cell next = worklist.get(0);
            worklist.remove(0);
            //Already searched
            if (next.equals(this.cells.get(this.x * this.y - 1))) {
                this.reconstruct(cameFromEdge, next);
                return;
            }
            //If it has not been searched
            else if (!(this.path.contains(next))) {
                this.path.add(next);
                for (Cell c : next.connection) {
                    //Choose how to treat the ArrayList (Queue or Stack)
                    if (algo.equals("b")) {
                        //queue
                        worklist.add(c);
                    }
                    else {
                        //stack
                        worklist.add(0, c);
                    }
                    //Decides to add to hashmap (Does not go backwards)
                    if (!(cameFromEdge.containsKey(c))) {
                        cameFromEdge.put(c, next);
                    }
                }
            }
        }
    }
    //Generate the final path
    void reconstruct(HashMap<Cell, Cell> hm, Cell cur) {
        Cell next = hm.get(cur);
        this.finalPath.add(cur);
        if (!(this.cells.get(0).equals(next))) {
            this.reconstruct(hm, next);
        }
    }
    // method to do kruskals algorithm
    void kruskal(ArrayList<Edge> all) {
        //Init
        HashMap<Cell, Cell> representatives = new HashMap<Cell, Cell>();
        Collections.sort(all, new WeightComp());
        for (Cell n : this.cells) {
            representatives.put(n, n);
        }
        //Algo
        while (this.treeCount(representatives) > 1) {
            Edge cur = all.get(0);
            Cell rep1 = this.find(cur.start, representatives);
            Cell rep2 = this.find(cur.end, representatives);
            //If the Edge's reps are the same
            if (rep1.equals(rep2)) {
                all.remove(cur);
                this.walls.add(cur);
            }
            //Union the edge
            else {
                this.union(representatives, rep1, rep2);
                all.remove(cur);
                this.span.add(cur);
            }
        }
        //Adds the rest of the edges to the walls
        this.walls.addAll(all);
    }
    //Counts the remaining trees
    int treeCount(HashMap<Cell, Cell> hm) {
        int i = 0;
        for (Map.Entry<Cell, Cell> s : hm.entrySet()) {
            if (s.getKey().equals(s.getValue())) {
                i += 1;
            }
        }
        return i;
    }
    // method to find a cell's representative
    Cell find(Cell x, HashMap<Cell, Cell> hm) {
        if (hm.get(x).equals(x)) {
            return x;
        }
        else {
            return this.find(hm.get(x), hm);
        }
    }
    // method to union a cell's representative hashmap
    void union(HashMap<Cell, Cell> rep, Cell x, Cell y) {
        rep.put(y, x);
    }
}

// class to hold examples and tests
class ExamplesCode {

    Cell a = new Cell(0, 0);
    Cell b = new Cell(0, 2);
    Cell c = new Cell(2, 2);
    Cell d = new Cell(1, 1);
    Cell e = new Cell(0, 1);
    Cell f = new Cell(0, 6);
    Edge e1 = new Edge(a, b, 30);
    Edge e2 = new Edge(b, f, 50);
    Edge e3 = new Edge(a, e, 50);
    Edge e4 = new Edge(b, e, 35);
    Edge e5 = new Edge(b, c, 40);
    Edge e6 = new Edge(f, d, 50);
    Edge e7 = new Edge(e, c, 15);
    Edge e8 = new Edge(c, d, 25);
    ArrayList<Cell> cellList = new ArrayList<Cell>();
    ArrayList<Edge> edgeList = new ArrayList<Edge>();
    ArrayList<Edge> goodTree = new ArrayList<Edge>();
    HashMap<Cell, Cell> hash2 = new HashMap<Cell, Cell>();
    ArrayList<Edge> walls = new ArrayList<Edge>();
    MazeWorld maze = new MazeWorld(3, 2, 10);
    Player p1 = new Player(new Cell(0, 0));
    //Initializes the data
    void initData() {
        this.a = new Cell(0, 0);
        this.a.connection = new ArrayList<Cell>();
        this.a.connection.add(this.b);
        this.b = new Cell(0, 2);
        this.b.connection = new ArrayList<Cell>();
        this.b.connection.add(this.c);
        this.d = new Cell(1, 1);
        this.d.connection = new ArrayList<Cell>();
        this.d.connection.add(this.e);
        this.e = new Cell(0, 1);
        this.f = new Cell(0, 6);
        this.c = new Cell(2, 2);
        this.c.connection = new ArrayList<Cell>();
        this.c.connection.add(this.d);
        this.e1 = new Edge(a, b, 30);
        this.e2 = new Edge(b, f, 50);
        this.e3 = new Edge(a, e, 50);
        this.e4 = new Edge(b, e, 35);
        this.e5 = new Edge(b, c, 40);
        this.e6 = new Edge(f, d, 50);
        this.e7 = new Edge(e, c, 15);
        this.e8 = new Edge(c, d, 25);
        this.cellList = new ArrayList<Cell>();
        this.cellList.add(this.a);
        this.cellList.add(this.b);
        this.cellList.add(this.c);
        this.cellList.add(this.d);
        this.cellList.add(this.e);
        this.cellList.add(this.f);
        this.edgeList = new ArrayList<Edge>();
        this.edgeList.add(this.e1);
        this.edgeList.add(this.e2);
        this.edgeList.add(this.e3);
        this.edgeList.add(this.e4);
        this.edgeList.add(this.e5);
        this.edgeList.add(this.e6);
        this.edgeList.add(this.e7);
        this.edgeList.add(this.e8);
        this.goodTree = new ArrayList<Edge>();
        this.goodTree.add(this.e7);
        this.goodTree.add(this.e8);
        this.goodTree.add(this.e1);
        this.goodTree.add(this.e4);
        this.goodTree.add(this.e2);
        this.walls = new ArrayList<Edge>();
        this.walls.add(this.e5);
        this.walls.add(this.e3);
        this.walls.add(this.e6);
        this.hash2 = new HashMap<Cell, Cell>();
        this.hash2.put(this.a, this.a);
        this.hash2.put(this.b, this.a);
        this.hash2.put(this.c, this.c);
        this.hash2.put(this.d, this.d);
        this.maze.cells = cellList;
        this.maze.span = new ArrayList<Edge>();
        this.maze.walls = new ArrayList<Edge>();
        this.maze.showFinal = true;
        this.maze.showPath = true;
        this.maze.drawPath = new ArrayList<Cell>();
        this.maze.path = new ArrayList<Cell>();
        this.maze.finalPath = new ArrayList<Cell>();
        this.maze.me = this.p1;
        this.p1 = new Player(this.maze.cells.get(0));
    }

    //Tests overrided equal
    boolean testEquals(Tester t) {
        initData();
        return t.checkExpect(a.equals((new Cell(0, 0))), true) &&
                t.checkExpect(a.equals(new Cell(0, 1)), false) &&
                t.checkExpect(a.equals(e1), false);
    }
    //Tests hashing function
    boolean testHash(Tester t) {
        initData();
        return t.checkExpect(a.hashCode(), 0) &&
                t.checkExpect(new Cell(0, 1).hashCode(), 1) &&
                t.checkExpect(new Cell(11, 100).hashCode(), 110100);
    }
    //Tests compare function
    boolean testCompare(Tester t) {
        initData();
        return t.checkExpect(new WeightComp().compare(this.e1, this.e2), -20) &&
                t.checkExpect(new WeightComp().compare(this.e2, this.e3), 0) &&
                t.checkExpect(new WeightComp().compare(this.e2, this.e1), 20);
    }
    //Tests kruskal's algo
    boolean testKruskal(Tester t) {
        initData();
        maze.kruskal(this.edgeList);
        return t.checkExpect(this.maze.span, this.goodTree) &&
                t.checkExpect(this.maze.walls, this.walls);
    }
    //Tests the tree count method
    boolean testTree(Tester t) {
        initData();
        return t.checkExpect(this.maze.treeCount(this.hash2), 3) &&
                t.checkExpect(this.maze.treeCount(new HashMap<Cell, Cell>()), 0);
    }
    //Tests find method
    boolean testFind(Tester t) {
        initData();
        return t.checkExpect(this.maze.find(this.a, hash2), this.a) &&
                t.checkExpect(this.maze.find(this.b, hash2), this.a);
    }
    //Tests the union method
    boolean testUnion(Tester t) {
        initData();
        this.maze.union(this.hash2, this.c, this.a);
        this.maze.union(this.hash2, this.d, this.c);
        return t.checkExpect(this.maze.find(this.a, this.hash2), this.d) &&
                t.checkExpect(this.maze.find(this.c, this.hash2), this.d);
    }

    void testPress(Tester t) {
        initData();
        this.maze.onKeyEvent("h");
        t.checkExpect(this.maze.showPath, false);
        this.maze.onKeyEvent("h");
        t.checkExpect(this.maze.showPath, true);
        initData();
        this.maze.onKeyEvent("H");
        t.checkExpect(this.maze.showFinal, false);
        this.maze.onKeyEvent("H");
        t.checkExpect(this.maze.showFinal, true);
        this.maze.path = this.cellList;
        this.maze.onKeyEvent("f");
        t.checkExpect(this.maze.path, new ArrayList<Cell>());
        t.checkExpect(this.maze.drawPath, this.cellList);
        initData();
        this.maze.onKeyEvent("down");
        t.checkExpect(this.maze.me.loc, this.b);
        this.maze.onKeyEvent("down");
        t.checkExpect(this.maze.me.loc, this.b);
        this.maze.onKeyEvent("right");
        t.checkExpect(this.maze.me.loc, this.c);
        this.maze.onKeyEvent("right");
        t.checkExpect(this.maze.me.loc, this.c);
        this.maze.onKeyEvent("up");
        t.checkExpect(this.maze.me.loc, this.d);
        this.maze.onKeyEvent("up");
        t.checkExpect(this.maze.me.loc, this.d);
        this.maze.onKeyEvent("left");
        t.checkExpect(this.maze.me.loc, this.e);
        this.maze.onKeyEvent("left");
        t.checkExpect(this.maze.me.loc, this.e);
        ArrayList<Cell> test = new ArrayList<Cell>();
        test.add(this.b);
        test.add(this.c);
        test.add(this.d);
        test.add(this.e);
        t.checkExpect(this.maze.playerPath, test);
    }

    void testSearchBSF(Tester t) {
        initData();
        this.maze.search("b");

        t.checkExpect(this.maze.path.get(0), this.a);
        t.checkExpect(this.maze.path.get(this.maze.path.size() - 1), this.e);
        t.checkExpect(this.maze.path.size(), 5);
    }

    void testSearchDSF(Tester t) {
        initData();
        this.maze.search("");

        t.checkExpect(this.maze.path.get(0), this.a);
        t.checkExpect(this.maze.path.get(this.maze.path.size() - 1), this.e);
        t.checkExpect(this.maze.path.size(), 5);
    }

    void testReconstruct(Tester t) {
        initData();
        this.maze.reconstruct(hash2, this.a);
        t.checkExpect(this.maze.finalPath.get(0), this.a);
        t.checkExpect(this.maze.finalPath.get(this.maze.finalPath.size() - 1), this.a);
        t.checkExpect(this.maze.finalPath.size(), 1);
    }

    public static void main(String[] args) {
        //EDIT THESE TO CHANGE SIZE OF MAZE
        int GRID_X = 50;
        int GRID_Y = 50;
        int CELL_SIZE = 15;
        MazeWorld m1 = new MazeWorld(GRID_X, GRID_Y, CELL_SIZE);
        m1.bigBang(GRID_X * CELL_SIZE, GRID_Y * CELL_SIZE, .01);
    }
}