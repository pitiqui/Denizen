package net.aufdemrand.denizen.objects;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Color;

import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class dColor implements dObject {

    final static Pattern rgbPattern = Pattern.compile("(\\d+),(\\d+),(\\d+)");
	
    //////////////////
    //    OBJECT FETCHER
    ////////////////
    
    /**
     * Gets a Color Object from a string form.
     *
     * @param string  the string
     * @return  a Color, or null if incorrectly formatted
     *
     */
    public static dColor valueOf(String string) {
    	
    	if (string.toUpperCase().matches("RANDOM")) {
    		
    		return new dColor(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
    	}
    	
    	Matcher m = rgbPattern.matcher(string);
    	
    	if (m.matches()) {
    		
    		return new dColor(aH.getIntegerFrom(m.group(1)), aH.getIntegerFrom(m.group(2)), aH.getIntegerFrom(m.group(3)));
    	}
    	
    	Field colorField = null;
    	
		try {
			colorField = Color.class.getField(string.toUpperCase());
		} catch (SecurityException e1) {
			dB.echoError("Security exception getting color field!");
		} catch (NoSuchFieldException e1) {
			dB.echoError("No such color field!");
		}

    	if (colorField != null) {
    	
    		return new dColor(colorField);
    	}
    			
        // No match
        return null;
    }
    
    /**
     * Determine whether a string is a valid color.
     *
     * @param string  the string
     * @return  true if matched, otherwise false
     *
     */
    public static boolean matches(String arg) {

    	if (arg.toUpperCase().matches("RANDOM"))
    		return true;
    	
    	Matcher m = rgbPattern.matcher(arg);
    	
    	if (m.matches())
    		return true;
    	
    	for (Field field : Color.class.getFields()) {
    		
    		if (arg.toUpperCase().matches(field.getName())) {
    			
    			return true;
    		}
    	}
    	
        return false;

    }
    
    
    ///////////////
    //   Constructors
    /////////////

    public dColor(int red, int green, int blue) {
        color = Color.fromRGB(red, green, blue);
    }
    
    public dColor(Field field) {
        try {
			color = (Color) field.get(null);
		} catch (IllegalArgumentException e) {
			dB.echoError("Illegal argument for color!");
		} catch (IllegalAccessException e) {
			dB.echoError("Illegal access for color!");
		}
    }

    public dColor(Color color) {
        this.color = color;
    }
    
    
    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    // Bukkit itemstack associated

    private Color color;

    public Color getColor() {
        return color;
    }
    

	@Override
	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String debug() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUnique() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String identify() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public dObject setPrefix(String prefix) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAttribute(Attribute attribute) {
		// TODO Auto-generated method stub
		return null;
	}

}
