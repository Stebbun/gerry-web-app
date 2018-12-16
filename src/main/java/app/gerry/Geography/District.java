package app.gerry.Geography;

import app.gerry.AlgorithmCore.ObjectiveFunction;
import app.gerry.Data.GeometricData;
import app.gerry.Data.Representative;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.*;
import java.util.stream.Collectors;

public class District {
    private int id;
    private String name;
    private Representative representative;
    private Set<Precinct> precincts;
    private Set<Chunk> chunks;
    private Map<Chunk, Integer> adjacentChunks;
    private Set<Chunk> borderChunks;
    private Geometry geometricData;
    private int population;
    private double ObjectiveValue;
    private State state;

    /**
     * Construct a district that contains one chunk (Seed district)
     * @param chunk
     */
    public District(Chunk chunk) {
        chunks = new HashSet<>();
        adjacentChunks = new HashMap<>();
        addChunk(chunk);

        //TODO: Add Chunk to chunks and update geometric data, etc...
    }

    public Chunk getRandomBorderChunk(){
        return null;
    }

    public Set<Chunk> getAdjacentChunks(){
        return adjacentChunks.keySet();
    }

    /**
     * Everytime we add a chunk update the district's:
     *  - adjacent chunks
     *  - boundary data
     *  - election data
     *  - population data
     * @param chunk
     */
    public void addChunk(Chunk chunk){
        chunks.add(chunk);
        chunk.setParentDistrict(this);
        updateAdjacentChunks(chunk, false);
        updateBoundaryData(chunk, false);
        updateElectionData(chunk);
        updatePopulationData(chunk, false);
    }

    public void removeChunk(Chunk chunk) {
        chunks.remove(chunk);
        chunk.setParentDistrict(null);
        updateAdjacentChunks(chunk, true);
        updateBoundaryData(chunk, true);
        updateElectionData(chunk);
        updatePopulationData(chunk, true);
    }

    private void updateAdjacentChunks(Chunk chunk, boolean isRemove) {
        List<Chunk> newAdjacentChunks = chunk.getAdjacentChunks().stream()
                .filter(c -> c.getParentDistrict().getId() != this.getId())
                .collect(Collectors.toList());
        if(isRemove){
            for(Chunk c : newAdjacentChunks) {
                int oldCount = adjacentChunks.get(c);
                adjacentChunks.put(c, oldCount - 1);
                if(oldCount == 1) {
                    adjacentChunks.remove(c);
                }
            }
            List<Chunk> oldAdjacentChunks = chunk.getAdjacentChunks().stream()
                    .filter(c -> c.getParentDistrict().getId() == this.getId())
                    .collect(Collectors.toList());
            adjacentChunks.put(chunk, oldAdjacentChunks.size());
        }
        else{
            for(Chunk c : newAdjacentChunks) {
                int oldCount = adjacentChunks.getOrDefault(c, 0);
                adjacentChunks.put(c, oldCount + 1);
            }
            adjacentChunks.remove(chunk);
        }
    }

    private void updateBoundaryData(Chunk chunk, boolean isRemove) {
        //If this is the first time we're setting the boundary data
        if(geometricData == null) {
            if(!isRemove)
                geometricData = chunk.getCummGeometricData();
            else
                System.out.println("Can't remove this district");
            return;
        }
        //Otherwise we have to incrementally adjust our boundary data with each chunk
        Geometry districtPolygon = this.geometricData;
        Geometry chunkPolygon = chunk.getCummGeometricData();
        Collection<Geometry> polygons = new ArrayList<>();
        polygons.add(districtPolygon);
        polygons.add(chunkPolygon);

        if(isRemove) {
            this.geometricData = districtPolygon.difference(chunkPolygon);
            return;
        }
        this.geometricData = new UnaryUnionOp(polygons).union();
    }

    private void updateElectionData(Chunk chunk) {
        //TODO
    }

    private void updatePopulationData(Chunk chunk, boolean isRemove) {
        if(isRemove) {
            this.population -= chunk.getCummPopulation();
            return;
        }
        this.population += chunk.getCummPopulation();
    }

    public Precinct getRandomBordering(){
        return null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setAdjacentChunks(Map<Chunk, Integer> adjacentChunks) {
        this.adjacentChunks = adjacentChunks;
    }

    public Set<Chunk> getBorderChunks() {
        return borderChunks;
    }

    public void setBorderChunks(Set<Chunk> borderChunks) {
        this.borderChunks = borderChunks;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public Representative getRepresentative() {
        return representative;
    }

    public void setRepresentative(Representative representative) {
        this.representative = representative;
    }

    public Set<Precinct> getPrecincts() {
        return precincts;
    }

    public void setPrecincts(Set<Precinct> precincts) {
        this.precincts = precincts;
    }

    public Set<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(Set<Chunk> chunks) {
        this.chunks = chunks;
    }

    public Geometry getGeometricData() {
        return geometricData;
    }

    public void setGeometricData(Geometry geometricData) {
        this.geometricData = geometricData;
    }

    public double getObjectiveValue() {
        return ObjectiveValue;
    }

    public void setObjectiveValue(double objectiveValue) {
        ObjectiveValue = objectiveValue;
    }
}
