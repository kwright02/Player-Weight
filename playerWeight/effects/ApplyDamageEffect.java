package playerWeight.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.gson.JsonObject;

import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import playerWeight.api.IWeightEffect;
import playerWeight.api.WeightRegistry;
import playerWeight.misc.JsonHelper;

public class ApplyDamageEffect extends BaseEffect
{
	Map<UUID, MutableInt> countdowns = new HashMap<UUID, MutableInt>();
	float amount;
	int cooldown;
	double percentLeft;
	
	public ApplyDamageEffect(JsonObject obj)
	{
		super(0, Double.MAX_VALUE, false, JsonHelper.getOrDefault(obj, "effectRidden", false));
		amount = obj.get("amount").getAsFloat();
		cooldown = obj.get("cooldown").getAsInt();
		percentLeft = obj.get("activation").getAsDouble() / 100;
	}
	
	@Override
	public void applyToPlayer(EntityPlayer player, double weight, double maxWeight, IAttributeInstance maxWeightInstance)
	{
		double scale = weight / maxWeight;
		if(scale < percentLeft)
		{
			return;
		}
		MutableInt counter = getCounter(player);
		counter.decrement();
		if(counter.getValue() < 0)
		{
			counter.setValue(cooldown);
			getLowestEntity(player).attackEntityFrom(DamageSource.MAGIC, amount);
		}
	}
	
	@Override
	public void onPlayerUnloaded(EntityPlayer player)
	{
		countdowns.remove(player.getUniqueID());
	}
	
	@Override
	public void clearEffects(EntityPlayer player)
	{
		countdowns.remove(player.getUniqueID());		
	}
	
	@Override
	public void onServerStop()
	{
		countdowns.clear();
	}
	
	public MutableInt getCounter(EntityPlayer player)
	{
		MutableInt data = countdowns.get(player.getUniqueID());
		if(data == null)
		{
			data = new MutableInt(cooldown);
			countdowns.put(player.getUniqueID(), data);
		}
		return data;
	}
	
	public static void register()
	{
		WeightRegistry.INSTANCE.registerWeightEffect("damage", new Function<JsonObject, IWeightEffect>(){
			@Override
			public IWeightEffect apply(JsonObject t)
			{
				return new ApplyDamageEffect(t);
			}
		});
	}
}