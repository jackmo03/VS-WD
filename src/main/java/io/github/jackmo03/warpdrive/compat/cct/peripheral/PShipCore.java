package io.github.jackmo03.warpdrive.compat.cct.peripheral;

import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.lua.LuaFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.impl.game.ShipTeleportDataImpl;
import dan200.computercraft.api.lua.LuaException;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PShipCore implements IPeripheral {
    private final BlockPos pos;
    private final Level world;

    public PShipCore(Level world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Nonnull
    @Override
    public String getType() {
        return "warpdrive_shipcore";
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return this == other || (other instanceof PShipCore && ((PShipCore) other).pos.equals(pos));
    }

    private LoadedServerShip getShip() {
        if (world instanceof ServerLevel) {
            return VSGameUtilsKt.getLoadedShipManagingPos((ServerLevel) world, pos);
        }
        return null;
    }

    @LuaFunction
    public final String getShipName() {
        LoadedServerShip ship = getShip();
        if (ship == null)
            return "No Ship";
        return ship.getSlug() != null ? ship.getSlug() : "Unnamed Ship";
    }

    @LuaFunction
    public final Long getId() {
        LoadedServerShip ship = getShip();
        return ship != null ? ship.getId() : null;
    }

    @LuaFunction
    public final Double getMass() {
        LoadedServerShip ship = getShip();
        return ship != null ? ship.getInertiaData().getMass() : null;
    }

    @LuaFunction
    public final Map<String, Double> getVelocity() {
        LoadedServerShip ship = getShip();
        if (ship == null)
            return null;
        Vector3dc vel = ship.getVelocity();
        Map<String, Double> map = new HashMap<>();
        map.put("x", vel.x());
        map.put("y", vel.y());
        map.put("z", vel.z());
        return map;
    }

    @LuaFunction
    public final Map<String, Object> getShipStatus() {
        Map<String, Object> status = new HashMap<>();
        LoadedServerShip ship = getShip();
        if (ship != null) {
            status.put("online", true);
            status.put("mass", ship.getInertiaData().getMass());
        } else {
            status.put("online", false);
        }
        return status;
    }

    @LuaFunction
    public final void applyBodyForce(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyBodyForce(ship.getId(), new Vector3d(x, y, z),
                    new Vector3d());
        }
    }

    @LuaFunction
    public final void applyWorldForce(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyWorldForce(ship.getId(), new Vector3d(x, y, z),
                    null);
        }
    }

    @LuaFunction
    public final void applyWorldTorque(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyWorldTorque(ship.getId(), new Vector3d(x, y, z));
        }
    }

    @LuaFunction
    public final void applyBodyTorque(double x, double y, double z) {
        LoadedServerShip ship = getShip();
        if (ship != null && world instanceof ServerLevel) {
            String dimId = VSGameUtilsKt.getDimensionId(world);
            ValkyrienSkiesMod.getOrCreateGTPA(dimId).applyBodyTorque(ship.getId(), new Vector3d(x, y, z));
        }
    }

    @LuaFunction
    public final void teleport(Map<?, ?> input) throws LuaException {
        LoadedServerShip ship = getShip();
        if (ship == null || !(world instanceof ServerLevel)) {
            throw new LuaException("This computer is not on a Ship!");
        }

        Vector3dc pos = ship.getTransform().getPositionInWorld();
        if (input.containsKey("pos")) {
            pos = getVectorFromTable(input, "pos");
        }

        Quaterniondc rot = ship.getTransform().getShipToWorldRotation();
        if (input.containsKey("rot")) {
            rot = getQuaternionFromTable(input, "rot").normalize(new Quaterniond());
        }

        Vector3dc vel = ship.getVelocity();
        if (input.containsKey("vel")) {
            vel = getVectorFromTable(input, "vel");
        }

        Vector3dc omega = ship.getAngularVelocity();
        if (input.containsKey("omega")) {
            omega = getVectorFromTable(input, "omega");
        }

        String dimension = null;
        if (input.containsKey("dimension")) {
            dimension = String.valueOf(input.get("dimension"));
        }

        Double scale = ship.getTransform().getShipToWorldScaling().x();
        if (input.containsKey("scale")) {
            scale = ((Number) input.get("scale")).doubleValue();
        }

        Vector3dc posInShip = ship.getTransform().getPositionInShip();

        ShipTeleportDataImpl teleportData = new ShipTeleportDataImpl(pos, rot, vel, omega, dimension, scale, posInShip);

        Object shipWorld = VSGameUtilsKt.getShipObjectWorld((ServerLevel) world);
        try {
            for (java.lang.reflect.Method m : shipWorld.getClass().getMethods()) {
                if (m.getName().equals("teleportShip") && m.getParameterCount() == 2) {
                    m.invoke(shipWorld, ship, teleportData);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Vector3dc getVectorFromTable(Map<?, ?> input, String section) throws LuaException {
        Object sectionObj = input.get(section);
        if (!(sectionObj instanceof Map)) {
            throw new LuaException("Malformed " + section);
        }
        Map<?, ?> table = (Map<?, ?>) sectionObj;
        double x = getDoubleFromTable(table, section, "x");
        double y = getDoubleFromTable(table, section, "y");
        double z = getDoubleFromTable(table, section, "z");
        return new Vector3d(x, y, z);
    }

    private Quaterniondc getQuaternionFromTable(Map<?, ?> input, String section) throws LuaException {
        Object sectionObj = input.get(section);
        if (!(sectionObj instanceof Map)) {
            throw new LuaException("Malformed " + section);
        }
        Map<?, ?> table = (Map<?, ?>) sectionObj;
        double x = getDoubleFromTable(table, section, "x");
        double y = getDoubleFromTable(table, section, "y");
        double z = getDoubleFromTable(table, section, "z");
        double w = getDoubleFromTable(table, section, "w");
        return new Quaterniond(x, y, z, w);
    }

    private double getDoubleFromTable(Map<?, ?> table, String section, String field) throws LuaException {
        Object val = table.get(field);
        if (!(val instanceof Number)) {
            throw new LuaException("Malformed " + field + " key of " + section);
        }
        return ((Number) val).doubleValue();
    }
}