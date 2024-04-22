import java.util.*;
import java.io.*;

class SmartGrid {
    List<List<Container>> Adjlist;
    private int places;
    int[] path;
    double[] voltpath;
    int temp_distance = 0;
    Dictionary<String, Integer> placeMap = new Hashtable<>();
    Dictionary<Integer, Double> voltdict = new Hashtable<>();
    String[]  place_names;

    SmartGrid(int vertices) {
        this.places = vertices;
        this.Adjlist = new ArrayList<>(vertices);
        this.path = new int[vertices + 1];
        this.voltpath = new double[vertices + 1];
        this.place_names=new String[vertices];

        voltdict.put(30, 33.0);
        voltdict.put(50, 48.0);
        voltdict.put(60, 50.0);
        voltdict.put(80, 60.0);
        voltdict.put(90, 66.0);
        voltdict.put(20, 0.44);
        voltdict.put(70, 49.0);
        voltdict.put(40, 40.0);
        voltdict.put(85, 63.0);
        voltdict.put(120, 345.0);
        voltdict.put(100, 345.0);
        voltdict.put(200, 400.0);
        voltdict.put(300, 450.0);
        voltdict.put(400, 490.0);
        voltdict.put(190, 380.0);
        voltdict.put(160, 360.0);

        placeMap.put("grid", 0);

        for (int i = 0; i < vertices; i++) {
            this.Adjlist.add(new ArrayList<>());
        }
    }

    static class Container {
        int source, destination, distance;
        double voltage;
        char from, to;

        public Container(int s, int d, int dist, double volt, char from, char to) {
            this.source = s;
            this.destination = d;
            this.distance = dist;
            this.voltage = volt;
            this.from = from;
            this.to = to;
        }
    }

    public void addEdge(int src, int des, int dist, double volt, char from, char to) {
        Container c = new Container(src, des, dist, volt, from, to);
        Adjlist.get(src).add(c);
    }

    public void removeEdge(int src, int des) {
        Iterator<Container> iterator = Adjlist.get(src).iterator();
        while (iterator.hasNext()) {
            Container neigh = iterator.next();
            if (neigh.destination == des) {
                iterator.remove();
                System.out.println("edge removed: " + src + "---" + des);
                break;
            }
        }
    }

    public int First_findpath(int src, int des, int p_src) {
        for (Container neighbor : Adjlist.get(src)) {
            temp_distance += neighbor.distance;
            path[p_src] = neighbor.destination;
            voltpath[p_src] = neighbor.voltage;
            if (neighbor.destination == des) {
                return temp_distance;
            }
            int dist = First_findpath(neighbor.destination, des, p_src + 1);
            if (dist == 0) {
                temp_distance -= neighbor.distance;
                path[p_src] = 0;
                voltpath[p_src] = 0;
            } else {
                return dist;
            }
        }
        return 0;
    }

    public int Second_findpath(int src, int des) {
        for (Container neighbor : Adjlist.get(src)) {
            temp_distance += neighbor.distance;

            if (neighbor.destination == des) {
                return temp_distance;
            }
            int dist = Second_findpath(neighbor.destination, des);
            if (dist == 0) {
                temp_distance -= neighbor.distance;
            } else {
                return dist;
            }
        }
        return 0;
    }

    public void GridOptimization(int s, int d, int dist) {
        for (int i = 0; i < places; i++) {
            path[i] = 0;
        }

        for (int i = 0; i < places; i++) {
            voltpath[i] = 0.0;
        }

        double getvolt = voltdict.get(dist);

        int min_distance = First_findpath(0, d, 1);
        temp_distance = 0;
        int org_dis = Second_findpath(0, s);
        int sum = org_dis + dist;

        if (sum > min_distance) {
            System.out.println("min-distance: " + "0--" + d + " is " + min_distance);
        } else {
            System.out.println("min-distance: " + "0--" + d + " is " + sum);
            int j = places - 1;
            while (j > 0) {
                if (path[j] == 0) {
                    j--;
                } else {
                    removeEdge(path[j - 1], path[j]);

                    addEdge(s, d, dist, getvolt, 'c', 'c');
                    System.out.println("edge added: " + s + "---" + d);

                    j = 0;
                }
            }
        }
    }

    public void printGrid() {
        System.out.println("S  d      dis      volt");
        for (int i = 0; i < places; i++) {
            for (Container neigh : Adjlist.get(i)) {
                System.out.println("(" + neigh.source + ", " + neigh.destination + ")" + " --> " + neigh.distance + " <--> " + neigh.voltage);
            }
        }
        Enumeration<String> keys = placeMap.keys();

        // Iterate over the keys and print key-value pairs
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            Integer value = placeMap.get(key);
            System.out.println("Key: " + key + ", Value: " + value);
        }
    }

    public boolean containsValue(String key){
        Enumeration<String> keys = placeMap.keys();
        while (keys.hasMoreElements()) {
            if (key.equals(keys.nextElement())) {
                return true;
            }
        }
        return false;
    }

    public void addplace(String name,int num){
        placeMap.put(name,num);
    }

    public void storeGraphToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < Adjlist.size(); i++) {
                List<Container> edges = Adjlist.get(i);
                for (Container edge : edges) {
                    writer.write(edge.source + "," + edge.destination + "," + edge.distance + "," +
                            edge.voltage + "," + edge.from + "," + edge.to+ "," +place_names[edge.source]+ "," +place_names[edge.destination]);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class grid_system {
    public static void main(String args[]) {
        Scanner s = new Scanner(System.in);
        SmartGrid g = new SmartGrid(countDestinationsFromFile());

        readPlacesFromFile(g);

        readEdgesFromFile(g);

        g.storeGraphToFile("initial_grid.txt");

        readGridOptimizationFromFile(g);

        g.printGrid();

        g.storeGraphToFile("Efficient_grid.txt");

        System.out.println("Give your Destination: ");

        String d = s.nextLine();

        CheckDestinationFromGrid(d);
    }

    private static int countDestinationsFromFile() {
        Set<String> destinations = new HashSet<>();

        try {
            Scanner scanner = new Scanner(new File("edges.txt"));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String destination = parts[1];
                destinations.add(destination);
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return destinations.size() + 1;
    }

    private static void readEdgesFromFile(SmartGrid g) {
        try {
            Scanner scanner = new Scanner(new File("edges.txt"));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                String des = parts[1];
                String src = parts[0];
                int dist = Integer.parseInt(parts[2]);
                double volt = Double.parseDouble(parts[3]);
                char from = parts[4].charAt(0);
                char to = parts[5].charAt(0);

                if (g.containsValue(src) && g.containsValue(des)) {
                    int a = g.placeMap.get(src);
                    int b = g.placeMap.get(des);

                    g.addEdge(a, b, dist, volt, from, to);

                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void readGridOptimizationFromFile(SmartGrid g) {
        try {
            Scanner scanner = new Scanner(new File("Additionaledges.txt"));

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                String src = parts[0];
                String des = parts[1];
                int dist = Integer.parseInt(parts[2]);
                if (g.containsValue(src) && g.containsValue(des)) {
                    int a = g.placeMap.get(src);
                    int b = g.placeMap.get(des);

                    g.GridOptimization(a, b, dist);
                }

            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static void readPlacesFromFile(SmartGrid g) {
        try {
            Scanner scanner = new Scanner(new File("edges.txt"));
            int num = 1;
            int i=1;
            g.place_names[0]="grid";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                String des = parts[1];

                if (g.containsValue(des)==false) {
                    g.addplace(des,num);
                    g.place_names[i]=des;
                    num+=1;
                    i+=1;
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void CheckDestinationFromGrid(String d) {
        try {
            Scanner scanner = new Scanner(new File("newdata.txt"));

            boolean found = false; // Add a boolean flag to track if the destination is found

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");

                if (parts.length >= 2) {
                    String des = parts[1];
                    String src = parts[0];
                    if (des.equals(d)) {
                        found = true; // Update the flag when the destination is found
                        break;
                    }
                }
            }

            if (found) {
                System.out.println("destination found: " + d);
            } else {
                System.out.println("destination not Found ");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



}
