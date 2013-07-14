package net.bytten.zosoko.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Taken from my other project: https://github.com/tcoxon/mylittleterrorist
 * and relicensed under Metazelda's license with permission.
 * 
 * Horribly messy (but compact and generic) A* implementation.
 */
public class AStar {
    public static interface IMap {
        public Collection<Coords> neighbors(Coords pos);
    }
    
    protected class DistanceComparator implements Comparator<Coords> {
        public int compare(Coords o1, Coords o2) {
            double s1 = fScore.get(o1),
                   s2 = fScore.get(o2);
            if (s1 < s2) return -1;
            if (s1 == s2) return 0;
            return 1;
        }
    }
    protected final DistanceComparator DISTCMP = new DistanceComparator();
    
    protected Map<Coords, Double> gScore = new TreeMap<Coords, Double>(),
                                 fScore = new TreeMap<Coords, Double>();
    protected Map<Coords, Coords> cameFrom = new TreeMap<Coords, Coords>();
    protected Set<Coords> closedSet = new TreeSet<Coords>();
    protected Queue<Coords> openSet = new PriorityQueue<Coords>(110, DISTCMP);
    protected IMap map;
    protected Set<Coords> from;
    protected Coords to;
    
    private static Set<Coords> setOf(Coords pos) {
        Set<Coords> set = new TreeSet<Coords>();
        set.add(pos);
        return set;
    }
    
    public AStar(IMap iMap, Coords from, Coords to) {
        this(iMap, setOf(from), to);
    }
    
    public AStar(IMap map, Set<Coords> from, Coords to) {
        this.map = map;
        this.from = from;
        this.to = to;
    }
    
    public Coords getCameFrom(Coords pos) {
        return cameFrom.get(pos);
    }
    
    protected double heuristicDistance(Coords pos) {
        // Manhattan distance heuristic
        return Math.abs(to.x - pos.x) + Math.abs(to.y - pos.y);
    }
    
    protected void updateFScore(Coords pos) {
        fScore.put(pos, gScore.get(pos) + heuristicDistance(pos));
    }
    
    public List<Coords> solve() {
        /* See this page for the algorithm:
         * http://en.wikipedia.org/wiki/A*_search_algorithm
         */
        for (Coords startPos: from) {
            gScore.put(startPos, 0.0);
            updateFScore(startPos);
            openSet.add(startPos);
        }
        
        while (!openSet.isEmpty()) {
            Coords current = openSet.remove();
            
            if (current.equals(to))
                return reconstructPath();
            
            closedSet.add(current);
            
            for (Coords neighbor: map.neighbors(current)) {
                
                if (closedSet.contains(neighbor))
                    continue;
                
                double dist = current.distance(neighbor);
                double g = gScore.get(current) + dist;
                
                if (!openSet.contains(neighbor) || g < gScore.get(neighbor)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, g);
                    updateFScore(neighbor);
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }
    
    protected Coords nextStep() {
        List<Coords> path = solve();
        if (path == null || path.size() == 0) return null;
        return path.get(0);
    }
    
    protected List<Coords> reconstructPath() {
        List<Coords> result = new ArrayList<Coords>();
        Coords current = to;
        while (current != null && !current.equals(from)) {
            result.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(result);
        return result;
    }

}
