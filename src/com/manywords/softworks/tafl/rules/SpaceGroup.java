package com.manywords.softworks.tafl.rules;

/**
 * SpecialSpace represents any fort square. That is,
 * the center square, the corner squares (or groups,
 * in case of Alea Evangelii), or forts in the middle
 * of the board (in the case of Alea Evangelii variants.)
 */
public enum SpaceGroup {
    NONE, // Normal space
    THRONE, // The center square
    ATTACKER_FORT, // Any fortress that isn't a corner
    DEFENDER_FORT,
    CORNER // Corner fortresses, potentially hostile
}
/*
    public static enum Type {
	}
	
	public SpaceGroup(List<Coord> spaces, Type type) {
		mSpaces = spaces;
		mType = type;
	}
	
	public SpaceGroup(Coord space, Type type) {
		mType = type;
		mSpaces = new ArrayList<Coord>(1);
		mSpaces.add(space);
	}
	
	private List<Coord> mSpaces;
	private Type mType;
	
	public boolean containsSpace(Space space) {
		if(mSpaces.contains(space)) return true;
		else return false;
	}
	
	public boolean containsTaflman(Taflman taflman) {
		for(Coord space : mSpaces) {
			if(space.occupier == taflman) {
				return true;
			}
		}
		
		return false;
	}
	
	public Type getType() {
		return mType;
	}
	
	public List<Space> getSpaces() {
		return mSpaces;
	}
}
*/