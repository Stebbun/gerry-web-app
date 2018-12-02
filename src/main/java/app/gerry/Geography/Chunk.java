package app.gerry.Geography;

import app.gerry.Data.ElectionData;
import app.gerry.Data.GeometricData;
import app.gerry.Enum.PoliticalSubdivision;

import java.util.List;
import java.util.Set;

public class Chunk {
    public int id;
    public String name;
    public List<Precinct> precients;
    public List<Chunk> adjacentChunks;
    public ElectionData cummElectionData;
    public GeometricData cummGeometricData;
    public int cummPopulation;
    public PoliticalSubdivision subdivision;
    public boolean isFinalized;
    public District parentDistrict;
    public boolean isBorderChunk;

    public Set<District> getAdjacentDistricts(){
        return null;
    }
    public Set<Chunk> getAdjacentChunks(){
        return null;
    }
    public void populateChunk(Precinct p){
        return;
    }
}