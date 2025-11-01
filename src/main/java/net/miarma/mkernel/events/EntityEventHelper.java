package net.miarma.mkernel.events;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class EntityEventHelper {
	
	private Player player;
	private Entity entity;
	private ItemStack itemStack;
	private ItemMeta itemMeta;
	private int damage = 0;
	private Damageable dMeta;
	private int maxDamage;
	private int amount;
	
	private EntityEventHelper(PlayerInteractEntityEvent event) {
		player = event.getPlayer();
		entity = event.getRightClicked();
		itemStack = player.getItemInHand();
		itemMeta = itemStack.getItemMeta();
		if (itemMeta instanceof Damageable) {
			dMeta = (Damageable) itemMeta;
			damage = dMeta.getDamage();
		}
		maxDamage = itemStack.getType().getMaxDurability();
		amount = itemStack.getAmount();
	}
	
	public static EntityEventHelper of(PlayerInteractEntityEvent event) {
		return new EntityEventHelper(event);
	}
	
	public void handleScissorsOnCreeper() {
		double r = Math.random();
		int n = (int) ((Math.random() + 1) * 1.25);
		if (r < 0.10) {
			this.entity.remove();
			this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
			this.player.playEffect(EntityEffect.FIREWORK_EXPLODE);
			this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.VINE, n));
			this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.GUNPOWDER, n));
			double r2 = Math.random();
			if (r2 < 0.30) {
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.CREEPER_HEAD, 1));
			}
			if (damage + 2 <= this.maxDamage) {
				dMeta.setDamage(damage + 2);
				this.itemStack.setItemMeta(dMeta);
			}
		} else {
			if (damage + 1 <= maxDamage) {
				dMeta.setDamage(damage + 1);
				this.itemStack.setItemMeta(dMeta);
			}
		}
	}
	
	public void handleScissorsOnPig() {
		double r = Math.random();
		if(r < 0.30) {
			if (((Ageable) this.entity).isAdult()) {
				int n = (int) ((Math.random() + 1) * 1.25);
				this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
				((Ageable) this.entity).setBaby();
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.PORKCHOP, n));

			}
		}
		if (damage + 2 <= this.maxDamage) {
			dMeta.setDamage(damage + 2);
			this.itemStack.setItemMeta(dMeta);
		}

	}
	
	public void handleScissorsOnCow() {
		double r = Math.random();
		if(r < 0.30) {
			if (((Ageable) this.entity).isAdult()) {
				int n = (int) ((Math.random() + 1) * 1.25);
				this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
				((Ageable) this.entity).setBaby();
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.BEEF, n));
			}
		}
		if (damage + 2 <= this.maxDamage) {
			dMeta.setDamage(damage + 2);
			this.itemStack.setItemMeta(dMeta);
		}
	}

	public void handleScissorsOnRabbit() {
		double r = Math.random();
		if(r < 0.30) {
			if (((Ageable) this.entity).isAdult()) {
				int n = (int) ((Math.random() + 1) * 1.25);
				this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
				((Ageable) this.entity).setBaby();
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.RABBIT, n));
			}
		}
		if (damage + 2 <= this.maxDamage) {
			dMeta.setDamage(damage + 2);
			this.itemStack.setItemMeta(dMeta);
		}
	}

	public void handleScissorsOnChicken() {
		double r = Math.random();
		if(r < 0.30) {
			if (((Ageable) this.entity).isAdult()) {
				int n = (int) ((Math.random() + 1) * 1.25);
				this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
				((Ageable) this.entity).setBaby();
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.CHICKEN, n));
			}
		}
		if (damage + 2 <= this.maxDamage) {
			dMeta.setDamage(damage + 2);
			this.itemStack.setItemMeta(dMeta);
		}
	}

	public void handleScissorsOnZombie() {
		double r = Math.random();
		if (r < 0.5) {
			if (((Ageable) this.entity).isAdult()) {
				int n = (int) ((Math.random() + 1) * 1.25);
				this.player.playSound(this.player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 1);
				this.entity.remove();
				Skeleton skeleton = (Skeleton) this.entity.getLocation().getWorld().spawnEntity(this.entity.getLocation(),
						EntityType.SKELETON);
				EntityEquipment equipment = skeleton.getEquipment();
				equipment.setItemInMainHand(null);
				this.player.getWorld().dropItemNaturally(this.entity.getLocation(), new ItemStack(Material.ROTTEN_FLESH, n));
				if (damage + 2 <= this.maxDamage) {
					dMeta.setDamage(damage + 2);
					this.itemStack.setItemMeta(dMeta);
				}

			}
		}
	}
	
	public void handleRottenFleshOnSkeleton() {
		int r = (int) (Math.random() * 100.);
		if(r >= 50) {
			if(this.amount >= 15) {
				this.entity.remove();
				this.itemStack.setAmount(amount - 15);
				Zombie zombie = (Zombie) this.entity.getLocation().getWorld()
						.spawnEntity(this.entity.getLocation(), EntityType.ZOMBIE);
				EntityEquipment equipment = zombie.getEquipment();
				equipment.setItemInMainHand(new ItemStack(Material.BOW));
			}
		}
	}
	
	public void handleTotemOnPillager() {
		this.itemStack.setAmount(0);
		double n = Math.random();
		if (n < 0.15) {
			this.entity.remove();
			Villager villager = (Villager) this.entity.getLocation().getWorld()
					.spawnEntity(this.entity.getLocation(),
					EntityType.VILLAGER);
			villager.setBaby();
			this.player.playSound(this.player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
			this.player.playEffect(EntityEffect.TOTEM_RESURRECT);
		}
	}
}
