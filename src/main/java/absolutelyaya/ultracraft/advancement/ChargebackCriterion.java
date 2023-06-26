package absolutelyaya.ultracraft.advancement;

import absolutelyaya.ultracraft.Ultracraft;
import absolutelyaya.ultracraft.entity.AbstractUltraHostileEntity;
import com.google.gson.JsonObject;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ChargebackCriterion extends AbstractCriterion<ChargebackCriterion.Conditions>
{
	static final Identifier ID = new Identifier(Ultracraft.MOD_ID, "chargeback");
	
	@Override
	protected Conditions conditionsFromJson(JsonObject obj, EntityPredicate.Extended playerPredicate, AdvancementEntityPredicateDeserializer predicateDeserializer)
	{
		return new Conditions(playerPredicate, EntityPredicate.Extended.getInJson(obj, "entity", predicateDeserializer));
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
		private final EntityPredicate.Extended victim;
		
		public Conditions(EntityPredicate.Extended player, EntityPredicate.Extended entity)
		{
			super(ID, player);
			victim = entity;
		}
		
		public static Conditions create(EntityPredicate.Extended entity)
		{
			return new Conditions(EntityPredicate.Extended.EMPTY, entity);
		}
		
		public boolean matches(LootContext victimLootContext)
		{
			return victim.test(victimLootContext);
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