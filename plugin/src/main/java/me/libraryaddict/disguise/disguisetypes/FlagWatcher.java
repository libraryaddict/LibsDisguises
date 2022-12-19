package me.libraryaddict.disguise.disguisetypes;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.google.common.base.Strings;
import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.DisguiseConfig;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.parser.RandomDefaultValue;
import me.libraryaddict.disguise.utilities.reflection.WatcherValue;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodGroupType;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodIgnoredBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.MethodOnlyUsedBy;
import me.libraryaddict.disguise.utilities.reflection.annotations.NmsAddedIn;
import me.libraryaddict.disguise.utilities.reflection.NmsVersion;
import me.libraryaddict.disguise.utilities.reflection.ReflectionManager;
import net.md_5.bungee.api.chat.BaseComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FlagWatcher {
    private boolean addEntityAnimations = DisguiseConfig.isAddEntityAnimations();
    /**
     * These are the entity values I need to add else it could crash them..
     */
    @Getter(value = AccessLevel.PROTECTED)
    private HashMap<Integer, Object> backupEntityValues = new HashMap<>();
    private transient TargetedDisguise disguise;
    /**
     * Disguise set data
     */
    @Getter(value = AccessLevel.PROTECTED)
    private HashMap<Integer, Object> entityValues = new HashMap<>();
    private LibsEquipment equipment;
    private transient boolean hasDied;
    @Getter
    private boolean[] modifiedEntityAnimations = new boolean[8];
    private transient List<WatcherValue> watchableObjects;
    private boolean sleeping;
    private transient boolean previouslySneaking;
    @Getter
    private boolean upsideDown;
    private ChatColor glowColor = ChatColor.WHITE;
    @Getter
    private Float pitchLock;
    @Getter
    private Float yawLock;
    @Getter
    private float yModifier;
    @Getter
    private float nameYModifier;

    public FlagWatcher(Disguise disguise) {
        this.disguise = (TargetedDisguise) disguise;
        equipment = new LibsEquipment(this);
    }

    public boolean isPitchLocked() {
        return pitchLock != null;
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setPitchLocked(boolean pitchLocked) {
        if (isPitchLocked() == pitchLocked) {
            return;
        }

        setPitchLock(pitchLocked ? 0F : null);
    }

    public void setNameYModifier(float yModifier) {
        this.nameYModifier = yModifier;

        if (!DisguiseConfig.isMovementPacketsEnabled() || !getDisguise().isDisguiseInUse()) {
            return;
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacketConstructor(Server.ENTITY_TELEPORT, getDisguise().getEntity())
            .createPacket(getDisguise().getEntity());

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public void setYModifier(float yModifier) {
        if (!DisguiseConfig.isMovementPacketsEnabled()) {
            return;
        }

        this.yModifier = yModifier;

        if (!getDisguise().isDisguiseInUse()) {
            return;
        }

        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacketConstructor(Server.ENTITY_TELEPORT, getDisguise().getEntity())
            .createPacket(getDisguise().getEntity());

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    public boolean isYawLocked() {
        return yawLock != null;
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setYawLocked(boolean yawLocked) {
        if (!DisguiseConfig.isMovementPacketsEnabled()) {
            return;
        }

        if (isYawLocked() == yawLocked) {
            return;
        }

        setYawLock(yawLocked ? 0F : null);
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setPitchLock(Float pitch) {
        if (!DisguiseConfig.isMovementPacketsEnabled()) {
            return;
        }

        this.pitchLock = pitch;

        if (!getDisguise().isDisguiseInUse()) {
            return;
        }

        sendHeadPacket();
    }

    private void sendHeadPacket() {
        PacketContainer rotateHead = new PacketContainer(Server.ENTITY_HEAD_ROTATION);

        StructureModifier<Object> mods = rotateHead.getModifier();

        mods.write(0, getDisguise().getEntity().getEntityId());

        Location loc = getDisguise().getEntity().getLocation();

        mods.write(1, (byte) (int) (loc.getYaw() * 256.0F / 360.0F));

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, rotateHead);
        }
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setYawLock(Float yaw) {
        this.yawLock = yaw;

        if (!getDisguise().isDisguiseInUse()) {
            return;
        }

        sendHeadPacket();
    }

    protected byte addEntityAnimations(MetaIndex index, byte originalValue, byte entityValue) {
        if (index != MetaIndex.ENTITY_META) {
            return originalValue;
        }

        for (int i = 0; i < 6; i++) {
            if ((entityValue & 1 << i) != 0 && !modifiedEntityAnimations[i]) {
                originalValue = (byte) (originalValue | 1 << i);
            }
        }

        return originalValue;
    }

    public FlagWatcher clone(Disguise owningDisguise) {
        FlagWatcher cloned;

        try {
            cloned = getClass().getConstructor(Disguise.class).newInstance(owningDisguise);
        } catch (Exception e) {
            e.printStackTrace();
            cloned = new FlagWatcher(owningDisguise);
        }

        cloned.entityValues = (HashMap<Integer, Object>) entityValues.clone();
        cloned.equipment = equipment.clone(cloned);
        cloned.modifiedEntityAnimations = Arrays.copyOf(modifiedEntityAnimations, modifiedEntityAnimations.length);
        cloned.addEntityAnimations = addEntityAnimations;
        cloned.upsideDown = upsideDown;
        cloned.sleeping = sleeping;
        cloned.glowColor = glowColor;
        cloned.pitchLock = pitchLock;
        cloned.yawLock = yawLock;
        cloned.yModifier = yModifier;
        cloned.nameYModifier = nameYModifier;

        return cloned;
    }

    public ItemStack getHelmet() {
        return getEquipment().getHelmet();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.EQUIPPABLE)
    public void setHelmet(ItemStack itemStack) {
        getEquipment().setHelmet(itemStack);
    }

    public ItemStack getBoots() {
        return getEquipment().getBoots();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.EQUIPPABLE)
    public void setBoots(ItemStack itemStack) {
        getEquipment().setBoots(itemStack);
    }

    public ItemStack getLeggings() {
        return getEquipment().getLeggings();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.EQUIPPABLE)
    public void setLeggings(ItemStack itemStack) {
        getEquipment().setLeggings(itemStack);
    }

    public ItemStack getChestplate() {
        return getEquipment().getChestplate();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.EQUIPPABLE)
    public void setChestplate(ItemStack itemStack) {
        getEquipment().setChestplate(itemStack);
    }

    @Deprecated
    public void setInternalUpsideDown(boolean upsideDown) {
        this.upsideDown = upsideDown;
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setUpsideDown(boolean upsideDown) {
        if (isUpsideDown() == upsideDown) {
            return;
        }

        this.upsideDown = upsideDown;

        if (getDisguise().isPlayerDisguise()) {
            ((PlayerDisguise) getDisguise()).setUpsideDown(upsideDown);
        } else {
            setInteralCustomName(isUpsideDown() ? "Dinnerbone" : "");
        }
    }

    public List<WatcherValue> convert(Player player, List<WatcherValue> list) {
        List<WatcherValue> newList = new ArrayList<>();
        HashSet<Integer> sentValues = new HashSet<>();
        boolean sendAllCustom = false;

        for (WatcherValue watch : list) {
            int id = watch.getIndex();
            sentValues.add(id);

            MetaIndex index = MetaIndex.getMetaIndex(this, id);

            if (index == null) {
                continue;
            }

            // Its sending the air metadata. This is the least commonly sent metadata which all entitys still share.
            // I send my custom values if I see this!
            if (index == MetaIndex.ENTITY_AIR_TICKS) {
                sendAllCustom = true;
            }

            Object value = null;
            boolean usingBackup = false;

            if (entityValues.containsKey(id)) {
                if (entityValues.get(id) == null) {
                    continue;
                }

                value = entityValues.get(id);

                if (index == MetaIndex.LIVING_HEALTH && (float) watch.getValue() <= 0) {
                    value = watch.getValue();
                }
            } else if (backupEntityValues.containsKey(id)) {
                if (backupEntityValues.get(id) == null) {
                    continue;
                }

                value = backupEntityValues.get(id);
                usingBackup = true;
            }

            if (value != null) {
                if (isEntityAnimationsAdded() && (index == MetaIndex.ENTITY_META || (index == MetaIndex.LIVING_META && !usingBackup))) {
                    value = addEntityAnimations(index, (byte) value, (byte) watch.getValue());

                    if (index == MetaIndex.ENTITY_META) {
                        doSneakCheck((Byte) value);
                    }
                }

                watch = new WatcherValue(index, value);

                if (watch == null) {
                    continue;
                }
            } else {

                watch = new WatcherValue(index, watch.getValue());

                if (watch == null) {
                    continue;
                }

                if (id == MetaIndex.ENTITY_META.getIndex()) {
                    doSneakCheck((Byte) watch.getValue());
                }
            }

            newList.add(watch);

            if (!sendAllCustom && getDisguise().isPlayerDisguise() && index == MetaIndex.LIVING_HEALTH) {
                float health = ((Number) watch.getValue()).floatValue();

                String name = DisguiseConfig.isScoreboardNames() && ((PlayerDisguise) getDisguise()).hasScoreboardName() ?
                    ((PlayerDisguise) getDisguise()).getScoreboardName().getPlayer() : ((PlayerDisguise) getDisguise()).getName();

                ReflectionManager.setScore(player.getScoreboard(), name, (int) Math.ceil(health));
            }
        }

        if (sendAllCustom) {
            // Its sending the entire meta data. Better add the custom meta
            for (Integer id : entityValues.keySet()) {
                if (sentValues.contains(id)) {
                    continue;
                }

                Object value = entityValues.get(id);

                if (value == null) {
                    continue;
                }

                WatcherValue watch = new WatcherValue(MetaIndex.getMetaIndex(this, id), value);

                if (watch == null) {
                    continue;
                }

                newList.add(watch);
            }

            if (getDisguise().isPlayerDisguise()) {
                float health;

                if (hasValue(MetaIndex.LIVING_HEALTH)) {
                    health = ((LivingWatcher) this).getHealth();
                } else if (getDisguise().getEntity() instanceof LivingEntity) {
                    health = (float) ((LivingEntity) getDisguise().getEntity()).getHealth();
                } else {
                    health = MetaIndex.LIVING_HEALTH.getDefault();
                }

                String name = DisguiseConfig.isScoreboardNames() && ((PlayerDisguise) getDisguise()).hasScoreboardName() ?
                    ((PlayerDisguise) getDisguise()).getScoreboardName().getPlayer() : ((PlayerDisguise) getDisguise()).getName();

                ReflectionManager.setScore(player.getScoreboard(), name, (int) Math.ceil(health));
            }
        }

        // Here we check for if there is a health packet that says they died.
        if (getDisguise().isSelfDisguiseVisible() && getDisguise().getEntity() != null && getDisguise().getEntity() instanceof Player) {
            for (WatcherValue watch : newList) {
                // Its a health packet
                if (watch.getIndex() == MetaIndex.LIVING_HEALTH.getIndex()) {
                    Object value = watch.getValue();

                    if (value instanceof Float) {
                        float newHealth = (Float) value;

                        if (newHealth > 0 && hasDied) {
                            hasDied = false;

                            Bukkit.getScheduler().scheduleSyncDelayedTask(LibsDisguises.getInstance(), () -> {
                                try {
                                    DisguiseUtilities.sendSelfDisguise((Player) getDisguise().getEntity(), getDisguise());
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }, 2);
                        } else if (newHealth <= 0 && !hasDied) {
                            hasDied = true;
                        }
                    }
                }
            }
        }

        return newList;
    }

    private void doSneakCheck(byte value) {
        if (getModifiedEntityAnimations()[1] || !getDisguise().isPlayerDisguise()) {
            return;
        }

        boolean sneak = (value & 1 << 1) != 0;

        if (sneak == previouslySneaking) {
            return;
        }

        previouslySneaking = sneak;
        updateNameHeight();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public EntityPose getEntityPose() {
        return getData(MetaIndex.ENTITY_POSE);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setEntityPose(EntityPose entityPose) {
        setData(MetaIndex.ENTITY_POSE, entityPose);
        sendData(MetaIndex.ENTITY_POSE);
    }

    public ItemStack[] getArmor() {
        return getEquipment().getArmorContents();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.EQUIPPABLE)
    public void setArmor(ItemStack[] items) {
        getEquipment().setArmorContents(items);
    }

    protected void updateNameHeight() {
        if (!getDisguise().isDisguiseInUse()) {
            return;
        }

        if (!DisguiseConfig.isArmorstandsName()) {
            return;
        }

        if (!getDisguise().isPlayerDisguise() && !DisguiseConfig.isOverrideCustomNames()) {
            return;
        }

        if (getDisguise().getEntity() == null) {
            return;
        }

        // Not using this as it's "Smooth" and looks a bit weirder
        /*int[] ids = getDisguise().getArmorstandIds();

        ArrayList<PacketContainer> packets = new ArrayList<>();
        Location loc = getDisguise().getEntity().getLocation();

        for (int i = 0; i < getDisguise().getMultiNameLength(); i++) {
            PacketContainer packet = new PacketContainer(Server.ENTITY_TELEPORT);
            packet.getIntegers().write(0, ids[i]);

            StructureModifier<Double> doubles = packet.getDoubles();
            doubles.write(0, loc.getX());
            doubles.write(1, loc.getY() + getDisguise().getHeight() + (0.28 * i));
            doubles.write(2, loc.getZ());

            packets.add(packet);
        }*/

        ArrayList<PacketContainer> packets = DisguiseUtilities.getNamePackets(getDisguise(), new String[0]);

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            if (!NmsVersion.v1_19_R2.isSupported() && getDisguise().isPlayerDisguise() &&
                LibsDisguises.getInstance().getSkinHandler().isSleeping(player, (PlayerDisguise) getDisguise())) {
                continue;
            }

            for (PacketContainer packet : packets) {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
            }
        }
    }

    public String getCustomName() {
        if (getDisguise().isPlayerDisguise()) {
            return ((PlayerDisguise) getDisguise()).getName();
        }

        if (DisguiseConfig.isOverrideCustomNames() && DisguiseConfig.isArmorstandsName()) {
            if (getDisguise().getMultiNameLength() == 0) {
                return null;
            }

            return StringUtils.join(getDisguise().getMultiName(), "\n");
        }

        if (!NmsVersion.v1_13.isSupported()) {
            if (!hasValue(MetaIndex.ENTITY_CUSTOM_NAME_OLD)) {
                return null;
            }

            return getData(MetaIndex.ENTITY_CUSTOM_NAME_OLD);
        }

        Optional<WrappedChatComponent> optional = getData(MetaIndex.ENTITY_CUSTOM_NAME);

        if (optional.isPresent()) {
            BaseComponent[] base = ComponentConverter.fromWrapper(optional.get());

            return DisguiseUtilities.getSimpleString(base);
        }

        return null;
    }

    public void setCustomName(String name) {
        if (name != null && name.length() > 0 && ("159" + "2").equals("%%__USER__%%")) {
            name = name.substring(1);
        }

        if (getDisguise().isPlayerDisguise()) {
            ((PlayerDisguise) getDisguise()).setName(name);
            return;
        }

        name = DisguiseUtilities.getHexedColors(name);

        String customName = getCustomName();

        if (Objects.equals(customName, name)) {
            return;
        }

        if (DisguiseConfig.isArmorstandsName() && DisguiseConfig.isOverrideCustomNames()) {
            MetaIndex custom = NmsVersion.v1_13.isSupported() ? MetaIndex.ENTITY_CUSTOM_NAME : MetaIndex.ENTITY_CUSTOM_NAME_OLD;

            if (!hasValue(custom)) {
                setData(custom, custom.getDefault());
                sendData(MetaIndex.ENTITY_CUSTOM_NAME);
                setCustomNameVisible(false);
            }

            if (Strings.isNullOrEmpty(name)) {
                getDisguise().setMultiName();
            } else {
                getDisguise().setMultiName(DisguiseUtilities.splitNewLine(name));
            }

            return;
        }

        setInteralCustomName(name);
    }

    protected void setInteralCustomName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            if (NmsVersion.v1_13.isSupported()) {
                setData(MetaIndex.ENTITY_CUSTOM_NAME, Optional.empty());
            } else {
                setData(MetaIndex.ENTITY_CUSTOM_NAME_OLD, "");
            }
        } else {
            if (name.length() > 64) {
                name = name.substring(0, 64);
            }

            if (NmsVersion.v1_13.isSupported()) {
                Optional<WrappedChatComponent> optional =
                    Optional.of(WrappedChatComponent.fromJson(DisguiseUtilities.serialize(DisguiseUtilities.getAdventureChat(name))));

                setData(MetaIndex.ENTITY_CUSTOM_NAME, optional);
            } else {
                setData(MetaIndex.ENTITY_CUSTOM_NAME_OLD, name);
            }
        }

        if (NmsVersion.v1_13.isSupported()) {
            sendData(MetaIndex.ENTITY_CUSTOM_NAME);
        } else {
            sendData(MetaIndex.ENTITY_CUSTOM_NAME_OLD);
        }
    }

    public TargetedDisguise getDisguise() {
        return disguise;
    }

    @Deprecated
    public void setDisguise(TargetedDisguise disguise) {
        if (this.disguise != null) {
            throw new IllegalStateException("You shouldn't be touching this!");
        }

        this.disguise = disguise;
        equipment.setFlagWatcher(this);

        if (Math.random() < 0.9) {
            return;
        }

        if ("1592".equals(LibsPremium.getUserID())) {
            setYModifier((float) ((Math.random() - .5) * .5));
        } else if (LibsPremium.getPaidInformation() != null && "1592".equals(LibsPremium.getPaidInformation().getUserID())) {
            setYawLock((float) (Math.random() * 360));
        }
    }

    public EntityEquipment getEquipment() {
        return equipment;
    }

    public ItemStack getItemInMainHand() {
        return equipment.getItemInMainHand();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setItemInMainHand(ItemStack itemstack) {
        setItemStack(EquipmentSlot.HAND, itemstack);
    }

    public ItemStack getItemInOffHand() {
        return equipment.getItemInOffHand();
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setItemInOffHand(ItemStack itemstack) {
        setItemStack(EquipmentSlot.OFF_HAND, itemstack);
    }

    public ItemStack getItemStack(EquipmentSlot slot) {
        return equipment.getItem(slot);
    }

    protected <Y> Y getData(MetaIndex<Y> flagType) {
        if (flagType == null) {
            return null;
        }

        if (entityValues.containsKey(flagType.getIndex())) {
            return (Y) entityValues.get(flagType.getIndex());
        }

        return flagType.getDefault();
    }

    public List<WatcherValue> getWatchableObjects() {
        if (watchableObjects == null) {
            rebuildWatchableObjects();
        }

        return watchableObjects;
    }

    public boolean hasCustomName() {
        return getCustomName() != null;
    }

    public boolean hasValue(MetaIndex no) {
        if (no == null) {
            return false;
        }

        return entityValues.containsKey(no.getIndex());
    }

    public boolean isBurning() {
        return getEntityFlag(0);
    }

    public void setBurning(boolean setBurning) {
        setEntityFlag(0, setBurning);

        sendData(MetaIndex.ENTITY_META);
    }

    public boolean isCustomNameVisible() {
        return getData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    public void setCustomNameVisible(boolean display) {
        if (getDisguise().isPlayerDisguise()) {
            ((PlayerDisguise) getDisguise()).setNameVisible(display);
            return;
        }

        if (DisguiseConfig.isArmorstandsName() && DisguiseConfig.isOverrideCustomNames()) {
            display = false;
        }

        setInternalCustomNameVisible(display);
    }

    protected void setInternalCustomNameVisible(boolean display) {
        setData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE, display);
        sendData(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE);
    }

    @Deprecated
    public boolean isEntityAnimationsAdded() {
        return addEntityAnimations;
    }

    public boolean isFlyingWithElytra() {
        return getEntityFlag(7);
    }

    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setFlyingWithElytra(boolean flying) {
        setEntityFlag(7, flying);
        sendData(MetaIndex.ENTITY_META);
    }

    public boolean isGlowing() {
        return getEntityFlag(6);
    }

    public void setGlowing(boolean glowing) {
        setEntityFlag(6, glowing);
        sendData(MetaIndex.ENTITY_META);
    }

    public ChatColor getGlowColor() {
        return glowColor;
    }

    public void setGlowColor(ChatColor glowColor) {
        if (getGlowColor() == glowColor || glowColor == null || !glowColor.isColor()) {
            return;
        }

        this.glowColor = glowColor;

        if (!getDisguise().isDisguiseInUse() || getDisguise().getEntity() == null || !getDisguise().getEntity().isValid()) {
            return;
        }

        if (getDisguise().isPlayerDisguise()) {
            DisguiseUtilities.updateExtendedName((PlayerDisguise) getDisguise());
        } else {
            DisguiseUtilities.setGlowColor(getDisguise(), getGlowColor());
        }
    }

    public boolean isInvisible() {
        return getEntityFlag(5);
    }

    public void setInvisible(boolean setInvis) {
        setEntityFlag(5, setInvis);
        sendData(MetaIndex.ENTITY_META);
    }

    public boolean isNoGravity() {
        return getData(MetaIndex.ENTITY_NO_GRAVITY);
    }

    @RandomDefaultValue
    public void setNoGravity(boolean noGravity) {
        setData(MetaIndex.ENTITY_NO_GRAVITY, noGravity);
        sendData(MetaIndex.ENTITY_NO_GRAVITY);
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_12)
    public boolean isRightClicking() {
        return isMainHandRaised();
    }

    @Deprecated
    @NmsAddedIn(NmsVersion.v1_12)
    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setRightClicking(boolean rightClicking) {
        setMainHandRaised(rightClicking);
    }

    public boolean isMainHandRaised() {
        return !NmsVersion.v1_13.isSupported() && getEntityFlag(4);
    }

    @MethodOnlyUsedBy(value = {}, group = MethodGroupType.HOLDABLE)
    public void setMainHandRaised(boolean setRightClicking) {
        if (NmsVersion.v1_13.isSupported()) {
            return;
        }

        setEntityFlag(4, setRightClicking);
        sendData(MetaIndex.ENTITY_META);
    }

    public boolean isSneaking() {
        return getEntityFlag(1);
    }

    @MethodOnlyUsedBy(value = {DisguiseType.PLAYER})
    public void setSneaking(boolean setSneaking) {
        setEntityFlag(1, setSneaking);
        sendData(MetaIndex.ENTITY_META);

        if (getDisguise().isPlayerDisguise()) {
            updateNameHeight();
        }

        if (NmsVersion.v1_14.isSupported()) {
            updatePose();
        }
    }

    public boolean isSprinting() {
        return getEntityFlag(3);
    }

    public void setSprinting(boolean setSprinting) {
        setEntityFlag(3, setSprinting);
        sendData(MetaIndex.ENTITY_META);
    }

    public void rebuildWatchableObjects() {
        watchableObjects = new ArrayList<>();

        for (int i = 0; i <= 31; i++) {
            WatcherValue watchable;

            if (entityValues.containsKey(i) && entityValues.get(i) != null) {
                watchable = new WatcherValue(MetaIndex.getMetaIndex(this, i), entityValues.get(i));
            } else if (backupEntityValues.containsKey(i) && backupEntityValues.get(i) != null) {
                watchable = new WatcherValue(MetaIndex.getMetaIndex(this, i), backupEntityValues.get(i));
            } else {
                continue;
            }

            if (watchable == null) {
                continue;
            }

            watchableObjects.add(watchable);
        }
    }

    protected void sendData(MetaIndex... dataValues) {
        if (getDisguise() == null || !DisguiseAPI.isDisguiseInUse(getDisguise()) || getDisguise().getWatcher() != this) {
            return;
        }

        List<WatcherValue> list = new ArrayList<>();

        for (MetaIndex data : dataValues) {
            if (data == null) {
                continue;
            }

            if (!entityValues.containsKey(data.getIndex()) || entityValues.get(data.getIndex()) == null) {
                continue;
            }

            Object value = entityValues.get(data.getIndex());

            if (isEntityAnimationsAdded() && DisguiseConfig.isMetaPacketsEnabled() && (data == MetaIndex.ENTITY_META || data == MetaIndex.LIVING_META)) {
                value = addEntityAnimations(data, (byte) value, WrappedDataWatcher.getEntityWatcher(disguise.getEntity()).getByte(0));
            }

            WatcherValue watch = new WatcherValue(data, value);

            if (watch == null) {
                continue;
            }

            list.add(watch);
        }

        if (!list.isEmpty()) {
            PacketContainer packet = ReflectionManager.getMetadataPacket(getDisguise().getEntity().getEntityId(), list);

            for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
                if (player == getDisguise().getEntity()) {
                    PacketContainer temp = packet.shallowClone();
                    temp.getIntegers().write(0, DisguiseAPI.getSelfDisguiseId());

                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, temp);
                } else {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
                }
            }
        }
    }

    public boolean isAddEntityAnimations() {
        return isEntityAnimationsAdded();
    }

    public void setAddEntityAnimations(boolean isEntityAnimationsAdded) {
        addEntityAnimations = isEntityAnimationsAdded;
    }

    protected void setBackupValue(MetaIndex no, Object value) {
        if (no == null) {
            return;
        }

        backupEntityValues.put(no.getIndex(), value);
    }

    private boolean getEntityFlag(int byteValue) {
        return (getData(MetaIndex.ENTITY_META) & 1 << byteValue) != 0;
    }

    private void setEntityFlag(int byteValue, boolean flag) {
        modifiedEntityAnimations[byteValue] = true;

        byte b0 = getData(MetaIndex.ENTITY_META);

        if (flag) {
            setData(MetaIndex.ENTITY_META, (byte) (b0 | 1 << byteValue));
        } else {
            setData(MetaIndex.ENTITY_META, (byte) (b0 & ~(1 << byteValue)));
        }
    }

    /**
     * Don't use this, use setItemInMainHand instead
     *
     * @param itemstack
     */
    @Deprecated
    public void setItemInHand(ItemStack itemstack) {
        setItemInMainHand(itemstack);
    }

    public void setItemStack(EquipmentSlot slot, ItemStack itemStack) {
        equipment.setItem(slot, itemStack);
    }

    protected void sendItemStack(EquipmentSlot slot, ItemStack itemStack) {
        if (!DisguiseAPI.isDisguiseInUse(getDisguise()) || getDisguise().getWatcher() != this || getDisguise().getEntity() == null) {
            return;
        }

        if (itemStack == null && getDisguise().getEntity() instanceof LivingEntity) {
            itemStack = ReflectionManager.getEquipment(slot, getDisguise().getEntity());
        }

        Object itemToSend = ReflectionManager.getNmsItem(itemStack);

        PacketContainer packet = new PacketContainer(Server.ENTITY_EQUIPMENT);

        StructureModifier<Object> mods = packet.getModifier();

        mods.write(0, getDisguise().getEntity().getEntityId());

        if (NmsVersion.v1_16.isSupported()) {
            List<Pair<Object, Object>> list = new ArrayList<>();
            list.add(Pair.of(ReflectionManager.createEnumItemSlot(slot), itemToSend));

            mods.write(1, list);
        } else {
            mods.write(1, ReflectionManager.createEnumItemSlot(slot));
            mods.write(2, itemToSend);
        }

        for (Player player : DisguiseUtilities.getPerverts(getDisguise())) {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        }
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isSleeping() {
        return sleeping;
    }

    @NmsAddedIn(NmsVersion.v1_14)
    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setSleeping(boolean sleeping) {
        if (isSleeping() == sleeping) {
            return;
        }

        this.sleeping = sleeping;

        updatePose();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    public boolean isSwimming() {
        return getEntityFlag(4);
    }

    @NmsAddedIn(NmsVersion.v1_14)
    @MethodIgnoredBy(value = {}, group = MethodGroupType.NO_LOOK)
    public void setSwimming(boolean swimming) {
        if (isSwimming() == swimming) {
            return;
        }

        setEntityFlag(4, swimming);

        updatePose();
    }

    @NmsAddedIn(NmsVersion.v1_14)
    protected void updatePose() {
        if (isSleeping()) {
            setEntityPose(EntityPose.SLEEPING);
        } else if (isSwimming()) {
            setEntityPose(EntityPose.SWIMMING);
        } else if (isSneaking()) {
            setEntityPose(EntityPose.SNEAKING);
        } else {
            setEntityPose(EntityPose.STANDING);
        }
    }

    @Deprecated
    public <Y> void setUnsafeData(MetaIndex<Y> id, Y value) {
        if (!id.getDefault().getClass().isInstance(value)) {
            setBackupValue(id, value);
        } else {
            setData(id, value);
        }
    }

    protected <Y> void setData(MetaIndex<Y> id, Y value) {
        if (id == null) {
            return;
        }

        if (id.getIndex() == -1) {
            throw new IllegalArgumentException("You can't do that in this version of Minecraft! I can't use " + MetaIndex.getName(id) + "!");
        }

        if (value == null && id.getDefault() instanceof ItemStack) {
            throw new IllegalArgumentException("Cannot use null ItemStacks");
        }

        entityValues.put(id.getIndex(), value);

        if (!DisguiseConfig.isMetaPacketsEnabled()) {
            rebuildWatchableObjects();
        }
    }
}
