package com.iontorrent.views.basic;


public class CoordFactory {
	
	public static int GRID=2;
	public static int COORD =3;
    
	public static CoordIF createCoord(int type, GuiCanvas canvas) {
		if (type == GRID){
			return new GridCoord(canvas);
		}
		else if (type == COORD){
			return new CrossCoord(canvas);
		}				
		else return null;	
	}
}
