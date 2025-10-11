package net.miarma.mkernel.events;

import static net.miarma.mkernel.util.Constants.ADMIN_STICK_KEY;
import static net.miarma.mkernel.util.Constants.FROZEN_KEY;
import static net.miarma.mkernel.util.Constants.SCISSORS_KEY;
import static net.miarma.mkernel.util.Constants.SPAWNER_BREAKER_KEY;
import static net.miarma.mkernel.util.Constants.SPECIAL_ITEM_TAG;
import static net.miarma.mkernel.util.Constants.SPY_KEY;
import static net.miarma.mkernel.util.Constants.VANISH_KEY;
import static net.miarma.mkernel.util.Constants.ZOMBIFICATION_POTION_KEY;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.CampfireStartEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import net.md_5.bungee.api.ChatColor;
import net.miarma.mkernel.MKernel;
import net.miarma.mkernel.common.minecraft.MinepacksAccessor;
import net.miarma.mkernel.common.minecraft.inventories.DisposalInventory;
import net.miarma.mkernel.common.minecraft.inventories.GlobalChest;
import net.miarma.mkernel.common.minecraft.inventories.InvseeInventory;
import net.miarma.mkernel.config.CustomConfigManager;
import net.miarma.mkernel.config.providers.ConfigProvider;
import net.miarma.mkernel.config.providers.MessageProvider;
import net.miarma.mkernel.tasks.LocationTrackerTask;
import net.miarma.mkernel.util.FileUtil;
import net.miarma.mkernel.util.InventoryUtil;
import net.miarma.mkernel.util.ItemUtil;
import net.miarma.mkernel.util.MessageUtil;
import net.miarma.mkernel.util.PlayerUtil;

public class EventListener {	
	public static void onEnable() {
		Bukkit.getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onInventoryOpen(InventoryOpenEvent event) {
				if (event.getInventory().equals(GlobalChest.getInv())) {
					GlobalChest.loadChest();
				}
			}

			@EventHandler
			public void onInventoryClose(InventoryCloseEvent event) {
				if (event.getInventory().equals(GlobalChest.getInv())) {
					GlobalChest.saveChest();
				} else if(event.getInventory().equals(DisposalInventory.getInv())) {
					DisposalInventory.getInv().clear();
				} else if(InvseeInventory.isInvseeInventory(event.getInventory())) {
					Player admin = (Player) event.getPlayer();

					if (!InvseeInventory.adminPlayerMap.containsKey(admin)) return;

					Player target = InvseeInventory.adminPlayerMap.remove(admin);
					Inventory view = event.getInventory();
					PlayerInventory tInv = target.getInventory();

					for (int i = 0; i < 36; i++) {
						tInv.setItem(i, view.getItem(i));
					}

					tInv.setHelmet(view.getItem(36));
					tInv.setChestplate(view.getItem(37));
					tInv.setLeggings(view.getItem(38));
					tInv.setBoots(view.getItem(39));
					tInv.setItemInOffHand(view.getItem(40));

				}
			}

			@EventHandler
			public void onPlayerDeath(PlayerDeathEvent event) throws IOException {
				Player player = event.getEntity();
				int playerLevel = player.getLevel();
				float playerExp = player.getExp();

				if (ConfigProvider.Modules.isDeathTitleEnabled()) {
					Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
					for (Player p : players) {
						p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1, 1);
						p.sendTitle(MessageUtil.parseColors(
								MessageUtil.parsePlaceholders(
										MessageProvider.Titles.getDeathSubtitle(),
										List.of("%player%"), List.of(player.getName()))),
								"", 30, 30, 30);
					}
				}

				if (ConfigProvider.Modules.isRecoverInventoryEnabled()) {
					Location deathLocation = player.getLocation();
					List<String> deathCoords = List.of(String.valueOf(deathLocation.getBlockX()),
							String.valueOf(deathLocation.getBlockY()), String.valueOf(deathLocation.getBlockZ()));
					Location playerSpawnPoint = player.getRespawnLocation();
					if (playerSpawnPoint == null) {
						playerSpawnPoint = player.getWorld().getSpawnLocation();
					}

					Collection<Player> players = (Collection<Player>) Bukkit.getOnlinePlayers();
					if(deathLocation.distance(playerSpawnPoint) <= ConfigProvider.Values.getRecInvSpawnDistance() ||
						PlayerUtil.playersNearRadius(player, players, ConfigProvider.Values.getRecInvPlayerRadius())) {
						MessageUtil.sendMessage(player, MessageProvider.Events.getOnDeathItemsNotRecovered(), true,
													 List.of("%x%", "%y%", "%z%"), deathCoords);
					} else {
						FileUtil.saveInventory(player);
						event.getDrops().clear();
						event.setDroppedExp(0);
						event.setKeepInventory(true);
						player.getInventory().setArmorContents(null);
						player.getInventory().clear();
						player.updateInventory();

						float xpLossOnDeath = ConfigProvider.Values.getXpLossOnDeath();
						int levelsToLose = Math.round(playerLevel * xpLossOnDeath);
						int newLevel = Math.max(0, playerLevel - levelsToLose);

						event.setNewLevel(newLevel);
						event.setNewExp((int) playerExp);

						MessageUtil.sendMessage(player, MessageProvider.Events.getOnDeathLostLevelsItems(), true,
													 List.of("%levels%"), List.of(String.valueOf(levelsToLose)));
					}
				}
			}

			@EventHandler
			public void onPlayerJoin(PlayerJoinEvent event) {
				Player player = event.getPlayer();
				if(ConfigProvider.Modules.isSpawnAtLobbyEnabled()) {
					if(Bukkit.getServer().getWorlds().stream()
							.map(World::getName)
							.map(String::toLowerCase)
							.anyMatch(w -> w.contains(ConfigProvider.Worlds.getLobby().name()))) {
						player.teleport(
								new Location(
									Bukkit.getWorld(ConfigProvider.Worlds.getLobby().name()),
									ConfigProvider.Worlds.getLobby().x(),
									ConfigProvider.Worlds.getLobby().y(),
									ConfigProvider.Worlds.getLobby().z(),
									ConfigProvider.Worlds.getLobby().yaw(),
									ConfigProvider.Worlds.getLobby().pitch()
								)
						);
					} else {
						MKernel.getPlugin(MKernel.class).getLogger()
								.log(Level.SEVERE, MessageUtil.formatMessageConsole(
										MessageProvider.Errors.lobbyDoesNotExist(), true));
					}
				}
				
				if (ConfigProvider.Modules.isJoinTitleEnabled()) {
					Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
					for (Player p : players) {
						p.sendTitle(
								MessageUtil.parseColors(MessageProvider.Titles.getTitleFormat())
								+ player.getName(),
								MessageUtil.parseColors(MessageProvider.Titles.getJoinSubtitle()), 30,
								30, 30);
					}
				}

				player.getPersistentDataContainer().set(VANISH_KEY, PersistentDataType.BOOLEAN, false);

				File f1 = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "warps/"
						+ event.getPlayer().getName() + ".yml");
				File f2 = new File(MKernel.PLUGIN.getDataFolder().getAbsolutePath(), "inventories/"
						+ event.getPlayer().getName() + ".yml");

				try {
					f1.createNewFile();
					f2.createNewFile();
				} catch (IOException e) {
					MKernel.LOGGER.severe("Error creating file for " + event.getPlayer().getName());
				}
			}

			@EventHandler
			public void onPlayerLeave(PlayerQuitEvent event) {
				if (ConfigProvider.Modules.isLeaveTitleEnabled()) {
					Player player = event.getPlayer();
					Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
					for (Player p : players) {
						p.sendTitle(
								MessageUtil.parseColors(MessageProvider.Titles.getTitleFormat())
								+ player.getName(),
								MessageUtil.parseColors(MessageProvider.Titles.getLeaveSubtitle()), 30,
								30, 30);
					}
				}
			}

			@EventHandler
			public void onRightClick(PlayerInteractEvent event) {
				if (!ConfigProvider.Modules.isHarvestOnRightClickEnabled()) {
		            return;
		        }
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
		            Block block = event.getClickedBlock();
		            Player player = event.getPlayer();
		            if (block != null) {
		                BlockEventHelper helper = BlockEventHelper.of(player, block);
		                Material blockType = block.getType();
		                switch (blockType) {
		                    case WHEAT: helper.handleWheat(); break;
		                    case POTATOES: helper.handlePotatoes(); break;
		                    case CARROTS: helper.handleCarrots(); break;
		                    case BEETROOTS: helper.handleBeetroots(); break;
		                    case COCOA: helper.handleCocoa(); break;
		                    case TORCHFLOWER_CROP: helper.handleTorchflower(); break;
		                    case PITCHER_CROP: helper.handlePitcher(); break;
		                    default: break;
		                }
		            }
		        }
			}

			@EventHandler
			public void onEntityRightClick(PlayerInteractEntityEvent event) {
				Entity e = event.getRightClicked();
				EquipmentSlot h = event.getHand();
				EntityEventHelper helper = EntityEventHelper.of(event);
				ItemStack item = event.getPlayer().getItemInHand();
				if(item == null || item.getType().equals(Material.AIR) || item.getAmount() == 0) {
					return;
				}
				
				AtomicReference<String> specialType = new AtomicReference<>();
				NBT.get(item, nbt -> {
					specialType.set(nbt.getString(SPECIAL_ITEM_TAG));
				});
				
				if(h.equals(EquipmentSlot.HAND)) {
					switch(e.getType()) {
						case PIG: 
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnPig();
							}
							break;
						case COW: 
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnCow();
							}
							break;
						case RABBIT:
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnRabbit();
							}
							break;
						case CHICKEN:
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnChicken();
							}
							break;
						case CREEPER:
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnCreeper();
							}
							break;
						case ZOMBIE: 
							if(SCISSORS_KEY.equals(specialType.get())) {
								helper.handleScissorsOnZombie();
							}
							break;
						case SKELETON:
							if(item.getType().equals(Material.ROTTEN_FLESH)) {
								helper.handleRottenFleshOnSkeleton();
							}
							break;
						case PILLAGER:
							if(item.getType().equals(Material.TOTEM_OF_UNDYING)) {
								helper.handleTotemOnPillager();
							}
							break;
						default: return;
					}	
				}
			}

			@EventHandler
			public void onEntityLeftClick(EntityDamageByEntityEvent event) {
				if (event.getDamager() instanceof Player player) {
                    ItemStack itemStack = player.getItemInHand();
					Material material = itemStack.getType();
					
					AtomicReference<String> specialType = new AtomicReference<>();
					NBT.get(itemStack, nbt -> {
						specialType.set(nbt.getString(SPECIAL_ITEM_TAG));
					});
					
					if (material.equals(Material.STICK) && 
							ADMIN_STICK_KEY.equals(specialType.get()) &&
							event.getEntity() instanceof LivingEntity) {
						((LivingEntity) event.getEntity()).setHealth(0);
					}
				}
			}

			@EventHandler
			public void onCampfireCook(BlockCookEvent event) {
				if (event.getBlock().getType() == Material.CAMPFIRE) {
					if (event.getSource().getType() == Material.ROTTEN_FLESH) {
						event.setResult(new ItemStack(ItemUtil.getBeefBoneWithProb()));
					}
				}
			}
			
			@EventHandler
			public void onBlockedWorldEnter(PlayerChangedWorldEvent event) {
				Player player = event.getPlayer();
				String world = player.getWorld().getName();

				CustomConfigManager worldBlockerConfigManager = new CustomConfigManager(MKernel.PLUGIN, "blockedWorlds.yml");
				worldBlockerConfigManager.reloadConfig();

				List<String> blockedWorlds = worldBlockerConfigManager.getConfig().getStringList("blockedWorlds");

				if (blockedWorlds.contains(world)) {
					Location loc = LocationTrackerTask.getPlayerRealTimeLocation(player);
					player.teleport(new Location(loc.getWorld(), loc.getX()-2, loc.getY(), loc.getZ()-2));

					MessageUtil.sendMessage(player, MessageProvider.Errors.worldIsBlocked(), true,
                                                List.of("%world%"), List.of(world));
				}
			}
			
			@EventHandler
			public void onChatMessage(AsyncPlayerChatEvent event) {
				Player player = event.getPlayer();
				PersistentDataContainer data = player.getPersistentDataContainer();

				if(ConfigProvider.Modules.isChatFormatEnabled()) {
					if(player.hasPermission(ConfigProvider.Permissions.getChatFormatPermission())) {
						event.setMessage(MessageUtil.parseColors(event.getMessage()));
					}
				}
			}

			@EventHandler
			public void onCommand(PlayerCommandPreprocessEvent event) {
				Player player = event.getPlayer();
				String message = event.getMessage();
				Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

				for(Player p:players) {
					PersistentDataContainer data = p.getPersistentDataContainer();
					boolean canSpy = Boolean.TRUE.equals(data.get(SPY_KEY, PersistentDataType.BOOLEAN));
					if(canSpy && !player.equals(p)) {
						MessageUtil.sendMessage(p, MessageProvider.Events.getOnCommandSpyMessage(), false,
												  List.of("%player%", "%message%"), List.of(player.getName(), message));
					}
				}
			}
						
			@EventHandler
			public void onAdminMessage(AsyncPlayerChatEvent event) {
				if(ConfigProvider.Modules.isAdminChatEnabled()) {
					if(event.getMessage().startsWith("#") &&
							event.getPlayer().hasPermission(ConfigProvider.Permissions.getAdminChatPermission())) {
						String msg = event.getMessage()
								.replaceFirst("#",
									MessageUtil.parseColors(MessageProvider.getAdminPrefix()+" "+
											"&7" + event.getPlayer().getName() + "&b: &r"))
								.replace("  ", " ");
						event.setCancelled(true);
						for(Player p:Bukkit.getOnlinePlayers()) {
							if(p.hasPermission(ConfigProvider.Permissions.getAdminChatPermission())) {
								p.sendRawMessage(msg);
							}
						}
					} else if(event.getMessage().startsWith("#") &&
							!event.getPlayer().hasPermission(ConfigProvider.Permissions.getAdminChatPermission())) {
						event.setCancelled(true);
						MessageUtil.sendMessage(event.getPlayer(), MessageProvider.Errors.noPermission(), true);
					}
				}
			}
			
			@EventHandler
			public void onMention(AsyncPlayerChatEvent event) {
				if(ConfigProvider.Modules.isMentionsEnabled()) {
					List<String> players = Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
					boolean containsPlayer = false;
					if(event.getPlayer().hasPermission(
							ConfigProvider.Permissions.getMentionsPermission()
					)) {
						Player victim = null;
						for(String s:players) {
							if(event.getMessage().contains("@"+s)) {
								victim = Bukkit.getServer().getPlayer(s);
								containsPlayer = true;
							}
						}
						if (victim != null && containsPlayer){
							String formattedMention = MessageUtil.parseColors(MessageProvider.Events.getOnMentionFormat()+victim.getName())+ChatColor.RESET;
							event.setMessage(event.getMessage().replace(victim.getName(), formattedMention));

							MessageUtil.sendMessage(victim, MessageProvider.Events.getOnMentionMessage(), true,
														List.of("%player%"), List.of(event.getPlayer().getName()));

							victim.playSound(victim, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
						}
					}
				}
			}

			@EventHandler
		    public void onBlockPlace(BlockPlaceEvent event) {
		        if (ConfigProvider.Modules.isAutoItemRefillEnabled()) {
		            ItemStack item = event.getItemInHand();
		            Material material = event.getBlockPlaced().getType();
		            Inventory playerInventory = event.getPlayer().getInventory();
		            Inventory playerBackpack;

		            if (item.getAmount() == 1) {
		                int itemCountInInventory = InventoryUtil.getItemCount(playerInventory, material);

		                if (itemCountInInventory > 1) {
		                    InventoryUtil.refillItem(event.getPlayer(), material, event.getHand());
		                } else {
							if(MinepacksAccessor.isLoaded()) {
								playerBackpack = MinepacksAccessor.getPlayerBackpackInventory(event.getPlayer());
								if (!(playerBackpack == null) && !playerBackpack.isEmpty()) {
									for (ItemStack i : playerBackpack.getContents()) {
										if (i != null && i.getType().equals(material)) {
											InventoryUtil.refillItemFromMinepack(event.getPlayer(), material, event.getHand());
											break;
										}
									}
								}
							}

                        }
		            }
		        }
		    }
			
			@EventHandler
			public void onBlockBreak(BlockBreakEvent event) {
				Player p = event.getPlayer();
				ItemStack item = p.getItemInHand();
				
				if(item == null)
					return;
				if(item.getType().equals(Material.AIR))
					return;
				if(item.getAmount() == 0)
					return;
				
				PlayerInventory inv = p.getInventory();
				Block block = event.getBlock();
				AtomicReference<String> specialType = new AtomicReference<>();
				NBT.get(item, nbt -> {
					specialType.set(nbt.getString(SPECIAL_ITEM_TAG));
				});
				
				if(specialType != null && SPAWNER_BREAKER_KEY.equals(specialType.get())) {
					int prob = (int) (Math.random() * 100);
					if(prob > ConfigProvider.Values.getSpawnerBreakerProbability()) {
						event.setCancelled(true);
						for(int i = 0; i < inv.getSize(); i++) {
							ItemStack invItem = inv.getItem(i);
			                if (invItem != null && 
			                		invItem.getType() != Material.AIR && 
			                		invItem.getAmount() != 0) {
			                    AtomicReference<String> specialTypeInv = new AtomicReference<>();
			    				NBT.get(invItem, nbt -> {
			    					specialTypeInv.set(nbt.getString(SPECIAL_ITEM_TAG));
			    				});
			                    if (specialType.get().equals(specialTypeInv.get())) {
			                        p.playSound(p, Sound.ENTITY_ITEM_BREAK, 1, 1);
			                        inv.clear(p.getInventory().getHeldItemSlot());
			                    }
			                }
						}
					} else {
						BlockState state = block.getState();
		                if (state instanceof CreatureSpawner) {
		                    CreatureSpawner spawner = (CreatureSpawner) state;
		                    ItemStack spawnerItem = new ItemStack(Material.SPAWNER);
		                    BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();
		                    meta.setBlockState(spawner);
		                    spawnerItem.setItemMeta(meta);
		                    block.getWorld().dropItem(block.getLocation(), spawnerItem);
		                    block.setType(Material.AIR); // Remove the spawner block
		                }
					}
				}
				
			}
			
			@EventHandler
			public void onItemBreak(PlayerItemBreakEvent event) {
				if(ConfigProvider.Modules.isAutoItemRefillEnabled()) {
					ItemStack item = event.getBrokenItem();
					Material material = item.getType();
					PlayerInventory playerInventory = event.getPlayer().getInventory();
		            Inventory playerBackpack;
		            EquipmentSlot hand = (playerInventory.getItemInMainHand().getType().equals(Material.AIR) || 
		                      !playerInventory.getItemInMainHand().getType().equals(item.getType())) ? 
		                      EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;

		            if(item.getAmount() == 1) {
		            	int itemCountInInventory = InventoryUtil.getItemCount(playerInventory, material);
		            	if (itemCountInInventory > 1) {
		                    InventoryUtil.refillItem(event.getPlayer(), material, hand);
		                } else {
							if(MinepacksAccessor.isLoaded()) {
								playerBackpack = MinepacksAccessor.getPlayerBackpackInventory(event.getPlayer());
								if(!(playerBackpack == null) && !playerBackpack.isEmpty()) {
									for (ItemStack i : playerBackpack.getContents()) {
										if (i != null && i.getType().equals(material)) {
											InventoryUtil.refillItemFromMinepack(event.getPlayer(), material, hand);
											break;
										}
									}
								}
							}
		                }
		            }
					
				}
			}
			
			@EventHandler
			public void onCampfireCook(CampfireStartEvent event) {
				if(!event.getBlock().getBlockData().getMaterial().equals(Material.SOUL_CAMPFIRE)) {
					 event.setTotalCookTime((int)Math.floor(1.5*event.getTotalCookTime()));
				}
			}
						
			@EventHandler
			public void onEntityDeath(EntityDeathEvent event) {
				Player p = event.getEntity().getKiller();
				if(p == null) {
					return;
				}
				
				ItemStack i = p.getItemInHand();
				if (i == null || !i.hasItemMeta() || i.getItemMeta().getEnchants().isEmpty()) {
			        return; 
			    }
				
				Enchantment ench = i.getEnchantments().entrySet().stream()
			            .map(entry -> entry.getKey())
			            .filter(e -> e == Enchantment.LOOTING)
			            .findFirst()
			            .orElse(null);
				if(ench == null) {
					return;
				}
				
				if (event.getEntityType().equals(EntityType.ZOMBIE) ||
			            event.getEntityType().equals(EntityType.ZOMBIE_VILLAGER)) {
			        int prob = (int) (Math.random() * 100);
			        if(p.getItemInHand().getEnchantments().keySet().stream()
			        		.anyMatch(e -> e.equals(ench))) {
			        	if (prob < 70) {
				            event.getEntity().getWorld()
				                .dropItem(event.getEntity().getLocation(), new ItemStack(Material.ZOMBIE_HEAD,1));
				        }
			        } else {
			        	if (prob < 50) {
				            event.getEntity().getWorld()
				                .dropItem(event.getEntity().getLocation(), new ItemStack(Material.ZOMBIE_HEAD,1));
				        }
			        }
			    }
			}
			
			@EventHandler
			public void onPotionSplash(PotionSplashEvent event) {
				ItemStack potion = event.getPotion().getItem();
				AtomicReference<String> specialType = new AtomicReference<>();
				NBT.get(potion, nbt -> {
					specialType.set(nbt.getString(SPECIAL_ITEM_TAG));
				});
				if(ZOMBIFICATION_POTION_KEY.equals(specialType.get())) {
					Collection<LivingEntity> entities = event.getAffectedEntities();
					int r = (int) (Math.random() * 100.);
					for(LivingEntity le : entities) {
						if(le instanceof Villager v) {
							if(r >= 30) {
								v.zombify();
							} else {
								v.setHealth(0.);
							}
						}
						if(le instanceof Player p) {
							p.addPotionEffect(
								new PotionEffect(PotionEffectType.POISON, 60*20, 3)
							);
						}
						
					}
				}
			}
			
			@EventHandler
			public void onBookWrite(PlayerEditBookEvent event) {
				if(ConfigProvider.Modules.isBookColorsEnabled()) {
					var bookMeta = event.getNewBookMeta();
			        var pages = bookMeta.getPages();
			        var newPages = new ArrayList<String>();
			        for (String page : pages) {
			            newPages.add(page.replace('&', '§'));
			        }
			        bookMeta.setPages(newPages);
			        event.setNewBookMeta(bookMeta);
				}
			}

			@EventHandler
			public void onPlayerMove(PlayerMoveEvent event) {
				Player player = event.getPlayer();
				Location loc = player.getLocation();

				PersistentDataContainer data = player.getPersistentDataContainer();
				boolean isFrozen = Boolean.TRUE.equals(data.get(FROZEN_KEY, PersistentDataType.BOOLEAN));

				if(isFrozen) {
					event.setCancelled(true);
					double x = loc.getX();
					double y = loc.getY();
					double z = loc.getZ();
					Location newLoc = new Location(loc.getWorld(), x, y, z);
					player.teleport(newLoc);
				}
			}

		}, MKernel.PLUGIN);
	}

	
}
