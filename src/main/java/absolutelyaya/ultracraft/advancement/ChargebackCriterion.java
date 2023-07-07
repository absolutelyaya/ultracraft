package absolutelyaya.ultracraft.advancement;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChargebackCriterion extends AbstractCriterion<ChargebackCriterion.Conditions>
{
	static final Identifier ID = new Identifier(Ultracraft.MOD_ID, "chargeback");
	
	@Override
	protected Conditions conditionsFromJson(JsonObject obj, LootContextPredicate playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer)
	{
		return new Conditions(playerPredicate, LootContextPredicate.fromJson("entity", predicateDeserializer, obj, LootContextTypes.ENTITY));
	}
	
	@Override
	public Identifier getId()
	{
		return ID;
	}
	
	public void trigger(ServerPlayerEntity player, AbstractUltraHostileEntity victim)
	{
		LootContext lootContext = EntityPredicate.createAdvancementEntityLootContext(player, victim);
		trigger(player, (conditions) -> conditions.matches(lootContext));
	}
	
	public static class Conditions extends AbstractCriterionConditions
	{
		private final LootContextPredicate victim;
		
		public Conditions(LootContextPredicate player, LootContextPredicate entity)
		{
			super(ID, player);
			victim = entity;
		}
		
		public static Conditions create(EntityPredicate entity)
		{
			return new Conditions(LootContextPredicate.EMPTY, EntityPredicate.asLootContextPredicate(entity));
		}
		
		public boolean matches(LootContext context)
		{
			return victim.test(context);
		}
		
		@Override
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer)
		{
			JsonObject json = super.toJson(predicateSerializer);
			json.add("entity", victim.toJson(predicateSerializer));
			return json;
		}
	}
}