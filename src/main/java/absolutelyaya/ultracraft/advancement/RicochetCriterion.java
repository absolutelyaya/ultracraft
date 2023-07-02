package absolutelyaya.ultracraft.advancement;

import absolutelyaya.ultracraft.Ultracraft;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class RicochetCriterion extends AbstractCriterion<RicochetCriterion.Conditions>
{
	static final Identifier ID = new Identifier(Ultracraft.MOD_ID, "ricochet");
	
	@Override
	protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer)
	{
		return new Conditions(obj.get("score").getAsInt());
	}
	
	@Override
	public Identifier getId()
	{
		return ID;
	}
	
	public void trigger(ServerPlayerEntity player, int score)
	{
		trigger(player, (conditions) -> conditions.matches(score));
	}
	
	public static class Conditions extends AbstractCriterionConditions
	{
		int score;
		
		public Conditions(int score)
		{
			super(ID, EntityPredicate.Extended.EMPTY);
			this.score = score;
		}
		
		public static RicochetCriterion.Conditions create(int score)
		{
			return new Conditions(score);
		}
		
		public boolean matches(int score)
		{
			return score >= this.score;
		}
		
		@Override
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer)
		{
			JsonObject json = super.toJson(predicateSerializer);
			json.add("score", new JsonPrimitive(score));
			return json;
		}
	}
}
