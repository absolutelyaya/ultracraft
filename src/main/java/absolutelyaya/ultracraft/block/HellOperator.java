package absolutelyaya.ultracraft.block;

public enum HellOperator
{
	IGNORE(179, 0, "screen.ultracraft.hell_observer.ignore"),
	MORE(179, 13, "screen.ultracraft.hell_observer.more"),
	LESS(179, 26, "screen.ultracraft.hell_observer.less"),
	EQUALS(179, 39, "screen.ultracraft.hell_observer.equals");
	
	public final int u, v;
	public final String translation;
	
	HellOperator(int u, int v, String translation)
	{
		this.u = u;
		this.v = v;
		this.translation = translation;
	}
	
	public boolean check(int a, int b)
	{
		return switch(this)
		{
			case IGNORE -> false;
			case MORE -> a > b;
			case LESS -> a < b;
			case EQUALS -> a == b;
		};
	}
}
