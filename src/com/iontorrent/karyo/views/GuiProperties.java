/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.utils.StringTools;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class GuiProperties {

	Map<String, String> prop;

	Map<String, String> alias;

	/*
	 * 
	 * controlCoverage.seg.TRACK_RENDERER=SCATTER_PLOT
	 * controlCoverage.seg.TRACK_COLOR=0,200,0
	 * controlCoverage.seg.TRACK_ALTCOLOR=200,0,0
	 * controlCoverage.seg.KARYO_RENDERER=SCATTER_PLOT
	 * controlCoverage.seg.KARYO_COLOR_GAIN=#TRACK_COLOR
	 * controlCoverage.seg.KARYO_COLOR_LOSS=#TRACK_ALTCOLOR
	 * controlCoverage.seg.KARYO_COLOR_NEUTRAL=0,0,178
	 * controlCoverage.seg.KARYO_CUTOFF_SCORE=1
         * controlCoverage.seg.KARYO_VISIBLE=true
	 * 
	 * may contain sample:
	 * 
	 * sample_controlCoverage.seq
	 */
	public GuiProperties(Map<String, String> tmpprop) {

		prop = new HashMap<String, String>();
		alias = new HashMap<String, String>();
		for (Iterator it = tmpprop.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			String KEY = key.toUpperCase();
			String val = tmpprop.get(key);
                       prop.put(KEY, val);
			if (KEY.indexOf(".ALIAS") > 0) {
				addAlias(KEY, val.toUpperCase());

			}
		}
		addReverse();
	}

	public GuiProperties(Properties tmpprop) {
		prop = new HashMap<String, String>();
		for (Iterator it = tmpprop.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			String KEY = key.toUpperCase();
			String val = tmpprop.getProperty(key);
			if (KEY.indexOf(".ALIAS") > 0) {
				addAlias(KEY, val.toUpperCase());

			} 
                        prop.put(KEY, val);
		}
	}

	public void put(String key, String val) {
		prop.put(key, val);
	}

	private void addAlias(String KEY, String NEWKEY) {
		int dot = KEY.lastIndexOf(".");
		KEY = KEY.substring(0, dot);
		// CN_Segments.seg.ALIAS=copy-number-segments.seg
		// CN_Segments.seg = copy-number-segments.seg
		p("Adding alias " + KEY + "->" + NEWKEY);
		alias.put(KEY, NEWKEY);
	}

	private void addReverse() {
		HashMap<String, String> newprop = new HashMap<String, String>();

		for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();

			String KEY = key.toUpperCase();
			// add the reverse for things like
			// copy-number-segments.seg.DISPLAY_NAME=Ploidy (seg)
			// get type = copy-number-segments
			// get ext = .seg
			// get disp = Ploidy (seg).
			// now for each type.ext, put in disp.ext
			if (KEY.endsWith("DISPLAY_NAME")) {
				int dot = key.indexOf(".");
				String type = key.substring(0, dot);
				String disp = prop.get(KEY);
				int dot1 = key.indexOf(".", dot + 1);
				String ext = "";
				if (dot1 > dot) {
					ext = key.substring(dot + 1, dot1);
				}
				String OLDKEY = type + "." + ext;
				OLDKEY = OLDKEY.toUpperCase();

				String NEWKEY = disp;
				if (ext.length() > 0) {
					NEWKEY = NEWKEY + "." + ext;
				}
				NEWKEY = NEWKEY.toUpperCase();
				// p("Found " + key + ". Got: " + type + "." + ext + "-> disp "
				// + disp + ",  new key: " + NEWKEY);
				for (Iterator it1 = prop.keySet().iterator(); it1.hasNext();) {
					String KEY1 = it1.next().toString().toUpperCase();
					// p("		Checking " + KEY1 + " for " + OLDKEY);
					if (KEY1.startsWith(OLDKEY)) {
						String val = prop.get(KEY1);
						int lastdot = KEY1.lastIndexOf(".");
						String QUAL = KEY1.substring(lastdot + 1);
						String NEWKEYQUAL = NEWKEY + "." + QUAL;
						if (!newprop.containsKey(NEWKEYQUAL)
								&& !prop.containsKey(NEWKEYQUAL)) {
							// p("				Adding "+NEWKEYQUAL+"="+val);
							newprop.put(NEWKEYQUAL, val);
						}
					}

				}

			}
		}
		for (Iterator it = newprop.keySet().iterator(); it.hasNext();) {
			String key = it.next().toString();
			prop.put(key, newprop.get(key));
		}

		// keys to upper case
	}

        public double getFilterValue(String sample, String name,
			String filetype) {
		double d = getDouble(getValueFor(sample, name, filetype, "FILTER_VALUE"));

		return d;
	}
	public String getFilterOperator(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "FILTER_OPERATOR");
                if (res != null) {
                    res = res.replace("greater", ">");
                    res = res.replace("gt", ">");
                    res = res.replace("lt", "<");
                    res = res.replace("less", "<");
                    res = res.replace("equals", "=");
                    res = res.replace("eq", "=");
                    res = res.replace("not", "<>");
                }
		// p("Score for " + name + ", " + filetype + "=" + res);
		return res;
	}
	public String getFilterKey(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "FILTER_KEY");
		// p("Score for " + name + ", " + filetype + "=" + res);
		return res;
	}
        public String getFilterMode(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "FILTER_MODE");
		// p("Score for " + name + ", " + filetype + "=" + res);
		return res;
	}
	public boolean setDisplayName(String sample, String name, String filetype,
			String value) {
		return setValueFor(sample, name, filetype, "DISPLAY_NAME", value);
	}

	public boolean setDescription(String sample, String name, String filetype,
			String value) {
		return setValueFor(sample, name, filetype, "DESCRIPTION", value);
	}

	public boolean setKaryoRenderer(String sample, String name,
			String filetype, String value) {
		return setValueFor(sample, name, filetype, "KARYO_RENDERER", value);
	}

	public String getDisplayName(String sample, String name, String filetype) {
		String s = getValueFor(sample, name, filetype, "DISPLAY_NAME");
                if (s != null) s = s.replace("_", " ");
                return s;
	}

	public String getDescription(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "DESCRIPTION");
	}
        public String getScoreLabel(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "SCORE_LABEL");
	}

	//
	public String getKaryoRenderer(String sample, String name, String filetype) {

		return getValueFor(sample, name, filetype, "KARYO_RENDERER", false);
	}

	public double getKaryoCutoffScore(String sample, String name,
			String filetype) {
		double d = getDouble(getValueFor(sample, name, filetype, "CUTOFF_SCORE"));

		return d;
	}

	public String getKaryoScoreName(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "KARYO_SCORE");
		// p("Score for " + name + ", " + filetype + "=" + res);
		return res;
	}
        
        public String getKaryoFilterScoreName(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "FILTER_KARYO_SCORE");
		// p("Score for " + name + ", " + filetype + "=" + res);
		return res;
	}

	public boolean setKaryoCutoffScore(String sample, String name,
			String filetype, double d) {
		return setValueFor(sample, name, filetype, "CUTOFF_SCORE", "" + d);
	}

	public boolean setKaryoScoreName(String sample, String name,
			String filetype, String value) {
		return setValueFor(sample, name, filetype, "KARYO_SCORE", value);
	}

	public Color getKaryoColorGain(String sample, String name, String filetype) {
		return getColor(getValueFor(sample, name, filetype, "KARYO_COLOR_GAIN"));
	}

	public int getTrackOrder(String sample, String name, String filetype) {
		return getInt(getValueFor(sample, name, filetype, "TRACK_ORDER"));
	}
        public boolean isKaryoVisible(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "KARYO_VISIBLE");
                if (res == null) return true;
                else return res.equalsIgnoreCase("true") || res.equals("1");
	}
         public boolean isShowSample(String sample, String name, String filetype) {
		String res = getValueFor(sample, name, filetype, "SHOW_SAMPLE");
                if (res == null) return true;
                else return res.equalsIgnoreCase("true") || res.equals("1");
	}

	public Color getKaryoColorLoss(String sample, String name, String filetype) {
		return getColor(getValueFor(sample, name, filetype, "KARYO_COLOR_LOSS"));
	}

	public Color getKaryoColorNeutral(String sample, String name,
			String filetype) {
		return getColor(getValueFor(sample, name, filetype,
				"KARYO_COLOR_NEUTRAL"));
	}

	public boolean setKaryoColorGain(String sample, String name,
			String filetype, Color color) {
		return setValueFor(sample, name, filetype, "KARYO_COLOR_GAIN",
				setColor(color));
	}

	public boolean setKaryoColorLoss(String sample, String name,
			String filetype, Color color) {
		return setValueFor(sample, name, filetype, "KARYO_COLOR_LOSS",
				setColor(color));
	}

	public boolean setKaryoColorNeutral(String sample, String name,
			String filetype, Color color) {
		return setValueFor(sample, name, filetype, "KARYO_COLOR_NEUTRAL",
				setColor(color));
	}

	public int getInt(String s) {
		int res = 0;
		try {
			res = Integer.parseInt(s);
		} catch (Exception e) {
			// p("Could not parse " + s + " as int");
		}
		return res;
	}

	public double getDouble(String s) {
		double res = Integer.MIN_VALUE;
		try {
			res = Double.parseDouble(s);
		} catch (Exception e) {
			// p("Could not parse " + s + " as double");
		}
		return res;
	}

	public String setColor(Color c) {
		return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	} 

	public String getKaryoTrackRenderer(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "KARYO_RENDERER", false);
	}

	public String getMode(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "TRACK_MODE");
	}

	public String getColorString(String s) {
		Color c = getColor(s);
		if (c == null)
			return "";
		else
			return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
	}

	public Color getColor(String s) {
		Color c = null;
		if (s == null) {
			// p("Got no key for getColor: " + s);
			return c;
		}
		s = s.toUpperCase().trim();
		if (s.indexOf(",") > 0) {
			// n,n,n
			ArrayList<Integer> nrs = StringTools.parseListtoInt(s, ",");
			c = new Color(nrs.get(0), nrs.get(1), nrs.get(2));
		} else if (s.indexOf("x") > 0) {
			// 0x
			c = Color.decode(s);
		} else {
			if (s.equalsIgnoreCase("RED")) {
				c = Color.red;
			} else if (s.equalsIgnoreCase("BLUE")) {
				c = Color.blue;
			} else if (s.equalsIgnoreCase("WHITE")) {
				c = Color.white;
			} else if (s.equalsIgnoreCase("ORANGE")) {
				c = Color.orange;
			} else if (s.equalsIgnoreCase("YELLOW")) {
				c = Color.YELLOW;
			} else if (s.equalsIgnoreCase("MAGENTA")) {
				c = Color.MAGENTA;
			} else if (s.equalsIgnoreCase("BROWN")) {
				c = Color.orange.darker();
			} else if (s.equalsIgnoreCase("CYAN")) {
				c = Color.CYAN;
			} else if (s.equalsIgnoreCase("GRAY")) {
				c = Color.GRAY;
			} else if (s.equalsIgnoreCase("LIGHTGRAY")) {
				c = Color.LIGHT_GRAY;
			} else if (s.equalsIgnoreCase("LIGHT_GRAY")) {
				c = Color.LIGHT_GRAY;
			} else if (s.equalsIgnoreCase("DARK_GRAY")) {
				c = Color.DARK_GRAY;
			} else if (s.equalsIgnoreCase("DARKGRAY")) {
				c = Color.DARK_GRAY;
			} else if (s.equalsIgnoreCase("PINK")) {
				c = Color.PINK;
			} else if (s.equalsIgnoreCase("GREEN")) {
				c = Color.GREEN;
			} else if (s.equalsIgnoreCase("LIGHT_GREEN")) {
				c = Color.GREEN.brighter();
			} else if (s.equalsIgnoreCase("LIGHT_RED")) {
				c = Color.RED.brighter();
			} else if (s.equalsIgnoreCase("LIGHT_BLUE")) {
				c = Color.BLUE.brighter();
			} else if (s.equalsIgnoreCase("RANDOM")) {
				c = randColor(150);
			}

		}
		// p("Color " + s + "-> " + c);
		return c;
	}

	private Color randColor(int min) {
		int avg = 0;
		int r = 0;
		int g = 0;
		int b = 0;
		while (avg < min) {
			r = (int) (Math.random() * 255);
			g = (int) (Math.random() * 255);
			b = (int) (Math.random() * 255);
			avg = (g + r + b) / 3;
		}
		return new Color(r, g, b);
	}

	public String getTrackAltColor(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "TRACK_ALTCOLOR");
	}

	public int getDataTrackHeight() {
		int h = 60;
		String v = prop.get("DATA_TRACK_HEIGHT");
		if (v != null) {
			try {
				h = Integer.parseInt(v);
			} catch (Exception e) {
				// p("Could not parse " + v + " for DATA_TRACK_HEIGHT");
			}
		}
		return h;
	}

	// public String getValueFor(String name, String filetype, String gui_key,
	// int count) {
	public String getValueFor(String sample, String name, String filetype,
			String gui_key) {
		return getValueFor(sample, name, filetype, gui_key, false);
	}

	public String getValueFor(String sample, String name, String filetype,
			String gui_key, boolean debug) {
		if (filetype.startsWith(".")) {
			filetype = filetype.substring(1);
		}
                if (name != null && sample != null && name.toLowerCase().startsWith(sample.toLowerCase())) {
                    name = name.substring(sample.length());
                }
                if (name.startsWith("_")) name = name.substring(1);
                name = name.trim();
		// int sep = name.indexOf("_");
		// if (sep > -1) {
		// name = name.substring(0, sep);
		// }
//                if (name.toLowerCase().startsWith("long") || sample.toLowerCase().startsWith("long")) {
//                    debug = true;
//                   
//                }
 		int sep = name.indexOf("."); 
		if (sep > -1) {
			name = name.substring(0, sep);
		}
		String val = null;
		if (sample != null) {
                        
			String key = sample + "_" + name + "." + filetype;
			val = getValueFor(key, gui_key, debug);
			if (debug)
				p("getValueFor, with sample" + key + ", =" + val);
		}
		if (val == null) {
			val = getValueFor(name + "." + filetype, gui_key, debug);
			if (debug)
				p("getValueFor " + name + "." + filetype + ", key=" + gui_key
						+ "=" + val);
		}

		if (val == null && filetype.equals("vcf")) {
			if (debug)
				p("Got nothing for " + name + "." + filetype + "." + gui_key
						+ ", will try GZ");
			val = getValueFor(name + ".gz", gui_key, debug);
		}
		if (val == null && filetype.equals("gz")) {
			if (debug)
				p("Got nothing for " + name + "." + filetype + "." + gui_key
						+ ", will try VCF");
			val = getValueFor(name + ".vcf", gui_key, debug);
		}
		if (val == null) {
			if (debug)
				p("Got nothing for " + name + "." + filetype + "." + gui_key
						+ ", will try just filetype");
			val = getValueFor(filetype, gui_key, debug);
		}
		if (val == null && filetype.equals("vcf")) {
			if (debug)
				p("Got nothing for " + name + "." + filetype + "." + gui_key
						+ ", will try just filetype");
			val = getValueFor("gz", gui_key, debug);
		}
		if (val == null && filetype.equals("gz")) {
			if (debug)
				p("Got nothing for " + name + "." + filetype + "." + gui_key
						+ ", will try just filetype");
			val = getValueFor("vcf", gui_key, debug);
		}
                if (debug) p(" got "+name+"."+filetype+"."+gui_key+"="+val);
		return val;
	}

	// public String getValueFor(String name, String filetype, String gui_key,
	// int count) {
	public boolean setValueFor(String sample, String name, String filetype,
			String gui_key, String value) {
		if (filetype.startsWith(".")) {
			filetype = filetype.substring(1);
		}
		// int sep = name.indexOf("_");
		// if (sep > -1) {
		// name = name.substring(0, sep);
		// }
		int sep = name.indexOf(".");
		if (sep > -1) {
			name = name.substring(0, sep);
		}
		boolean found = false;
		if (sample != null) {
			found = setValueFor(sample + "_" + name + "." + filetype, gui_key,
					value);
		}
		if (!found) {
			found = setValueFor(name + "." + filetype, gui_key, value);
		}
		if (!found && filetype.equals("vcf")) {
			// p("Got nothing for " + name + "." + filetype + "." + gui_key +
			// ", will try GZ");
			found = setValueFor(name + ".gz", gui_key, value);
		}
		if (!found && filetype.equals("gz")) {
			// p("Got nothing for " + name + "." + filetype + "." + gui_key +
			// ", will try VCF");
			found = setValueFor(name + ".vcf", gui_key, value);
		}
		if (!found) {
			// p("Got nothing for " + name + "." + filetype + "." + gui_key +
			// ", will try just filetype");
			found = setValueFor(filetype, gui_key, value);
		}
		if (!found && filetype.equals("vcf")) {
			// p("Got nothing for " + name + "." + filetype + "." + gui_key +
			// ", will try just filetype");
			found = setValueFor("gz", gui_key, value);
		}
		if (!found && filetype.equals("gz")) {
			// p("Got nothing for " + name + "." + filetype + "." + gui_key +
			// ", will try just filetype");
			found = setValueFor("vcf", gui_key, value);
		}
		return found;
	}

	public boolean setValueFor(String name, String gui_key, String value) {
		// first get value for name and file type
		gui_key = gui_key.toUpperCase();

		String key = name + "." + gui_key;

		key = key.toUpperCase().trim();
		String oldval = get(key);
		if (oldval != null && oldval.startsWith("#")) {

			String guiother = oldval.substring(1).toUpperCase();
			String keyother = name + "." + guiother;
			keyother = keyother.toUpperCase().trim();
			// p("Value is a variable, will attach " + name.toUpperCase() +
			// " first: " + keyother);
			oldval = get(keyother);
		}

		if (oldval != null) {
			prop.put(key, value);
			p("Replacing " + oldval + " with " + key + "=" + value);
			return true;
		} else
			return false;
	}

	public String getValueFor(String name, String gui_key, boolean debug) {
		// first get value for name and file type
		gui_key = gui_key.toUpperCase();

		name = name.toUpperCase();
		String key = name + "." + gui_key;

		key = key.toUpperCase().trim();
		String val = get(key);
		if (val == null) {
			// checking alias
			String al = alias.get(name);
                        //String al = alias.get(name);
                        if (debug) p("Got no value for :"+key+", will try alias for "+name+": "+al);
			if (al != null && !al.equalsIgnoreCase(name)) {

				key = al + "." + gui_key;
				p("+++Found alias " + al + " for " + name + ", using " + key);
				val = get(key);
			}
                        if (debug) p("Got no alias for for "+name+": "+al);
		}
		if (val != null && val.startsWith("#")) {

			String guiother = val.substring(1).toUpperCase();
			String keyother = name + "." + guiother;
			keyother = keyother.toUpperCase().trim();
			// p("Value is a variable, will attach " + name.toUpperCase() +
			// " first: " + keyother);
			val = get(keyother);
		}

		return val;
	}

	private void p(String s) {
		Logger.getLogger("GuiProperties").info(s);
	}

	public String get(String key) {
		key = key.toUpperCase().trim();
		String v = prop.get(key);
		// p("Get " + key + " is " + v);
		return v;
	}

	public Iterator<String> keys() {
		// TODO Auto-generated method stub
		return prop.keySet().iterator();
	}

	public boolean containsKey(String key) {
		return prop.containsKey(key);
	}

	public String getTrackColor(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "TRACK_COLOR");
	}

	public String getTrackMidColor(String sample, String name, String filetype) {
		return getValueFor(sample, name, filetype, "TRACK_MIDCOLOR");
	}
}
