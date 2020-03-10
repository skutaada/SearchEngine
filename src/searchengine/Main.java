package searchengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        SearchEngine se = new SearchEngine(args);

        boolean exit = false;

        while (!exit) {
            System.out.println("=== Menu ===");
            System.out.println("1. Find a person");
            System.out.println("2. Print all persons");
            System.out.println("0. Exit.");
            String answer = SearchEngine.sc.nextLine();

            switch (answer) {
                case "1":
                    se.setSearchStrategy();
                    se.search();
                    break;
                case "2":
                    se.printDatabase();
                    break;
                case "0":
                    System.out.println("Bye!");
                    exit = true;
                    break;
                default:
                    System.out.println("Incorrect option! Try again.");
                    break;
            }
        }
    }
}

class SearchEngine {

    protected List<String> database;
    protected Map<String, HashSet<Integer>> map;
    protected SearchStrategy searchStrategy;

    public static Scanner sc = new Scanner(System.in);

    public SearchEngine(String[] args) {
        setDatabase(args);
        setMap();
    }

    public void setSearchStrategy() {

        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        String strategy = sc.nextLine();

        switch (strategy) {
            case "ALL":
                searchStrategy = new AllSearch();
                break;
            case "ANY":
                searchStrategy = new AnySearch();
                break;
            case "NONE":
                searchStrategy = new NoneSearch();
                break;
            default:
                System.out.println("Wrong strategy");
                break;
        }
    }

    public void search() {
        System.out.println("Enter a name or email to search all suitable people.");
        List<String> query = List.of(sc.nextLine().split(" "));
        Set<Integer> foundPeople = searchStrategy.applySearchStrategy(map, query);
        if (foundPeople.isEmpty()) {
            System.out.println("No suitable people found.");
        } else {
            System.out.printf("%d persons found:\n", foundPeople.size());
            for (Integer index : foundPeople) {
                System.out.println(database.get(index));
            }
        }
    }

    private void setDatabase(String[] args) {
        database = new ArrayList<>();
        File file = new File(args[1]);

        try (Scanner fileReader = new Scanner(file)) {
            while (fileReader.hasNext()) {
                database.add(fileReader.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }

    private void setMap() {
        map = new HashMap<>();

        for (int i = 0; i < database.size(); i++) {
            String[] strings = database.get(i).split(" ");
            for (String string : strings) {
                HashSet<Integer> occurrences = map.getOrDefault(string.toLowerCase(), new HashSet<>());
                occurrences.add(i);
                map.put(string.toLowerCase(), occurrences);
            }
        }
    }

    public void printDatabase() {
        System.out.println("=== List of people ===");
        for (String data : database) {
            System.out.println(data);
        }
    }
}

abstract class SearchStrategy {

    abstract public Set<Integer> applySearchStrategy(Map<String, HashSet<Integer>> map, List<String> query);

}

class AnySearch extends SearchStrategy {

    @Override
    public Set<Integer> applySearchStrategy(Map<String, HashSet<Integer>> map, List<String> query) {
        Set<Integer> foundPeople = new HashSet<>();

        for (String person : query) {
            foundPeople.addAll(map.getOrDefault(person, new HashSet<>()));
        }

        return foundPeople;
    }
}

class AllSearch extends SearchStrategy {

    @Override
    public Set<Integer> applySearchStrategy(Map<String, HashSet<Integer>> map, List<String> query) {
        Set<Integer> foundPeople = new HashSet<>();

        for (String person : query) {
            if (foundPeople.isEmpty()) {
                foundPeople.addAll(map.getOrDefault(person, new HashSet<>()));
            } else {
                foundPeople.retainAll(map.getOrDefault(person, new HashSet<>()));
            }
        }

        return foundPeople;
    }
}

class NoneSearch extends SearchStrategy {

    @Override
    public Set<Integer> applySearchStrategy(Map<String, HashSet<Integer>> map, List<String> query) {
        Set<Integer> foundPeople = new HashSet<>();

        Set<Integer> banSet = new HashSet<>();

        for (String mapPerson : map.keySet()) {
            if (query.contains(mapPerson)) {
                banSet.addAll(map.get(mapPerson));
            } else {
                foundPeople.addAll(map.get(mapPerson));
            }
        }

        foundPeople.removeAll(banSet);

        return foundPeople;
    }
}
