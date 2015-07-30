package configgen;

public abstract class FlatStream {
	public abstract boolean isSectionEnd();	
	public abstract boolean getBool();	
	public abstract int getInt();	
	public abstract long getLong();
	public abstract float getFloat();
	public abstract String getString();
	
	public abstract FlatStream putBool(boolean x);
	public abstract FlatStream putInt(int x);
	public abstract FlatStream putLong(long x);
	public abstract FlatStream putFloat(float x);
	public abstract FlatStream putString(String x);
	public abstract FlatStream putSectionEnd();
	public abstract void checkLineEnd();
}
