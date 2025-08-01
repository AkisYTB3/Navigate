package org.notionsmp.navigate;

import com.destroystokyo.paper.MaterialSetTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.*;

public class AStarPathfinder {

    private static final Set<Material> PASSABLE_MATERIALS = EnumSet.of(
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.WATER, Material.LAVA,
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.COBWEB, Material.TRIPWIRE,
            Material.TORCH, Material.WALL_TORCH, Material.REDSTONE_TORCH, Material.REDSTONE_WALL_TORCH,
            Material.LEVER,
            Material.SUGAR_CANE, Material.KELP, Material.SEAGRASS, Material.TALL_SEAGRASS,
            Material.REDSTONE_WIRE, Material.REPEATER, Material.COMPARATOR,
            Material.SNOW, Material.BAMBOO_SAPLING,
            Material.SWEET_BERRY_BUSH, Material.GLOW_LICHEN, Material.SCULK_VEIN,
            Material.SMALL_DRIPLEAF
    );

    private static final int MAX_PATHFINDING_ITERATIONS = 50000;
    private final boolean safeMode;

    public AStarPathfinder(boolean safeMode) {
        this.safeMode = safeMode;
        PASSABLE_MATERIALS.addAll(MaterialSetTag.BUTTONS.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.FLOWERS.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.FLOWER_POTS.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.PRESSURE_PLATES.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.WOOL_CARPETS.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.RAILS.getValues());
        PASSABLE_MATERIALS.addAll(MaterialSetTag.CLIMBABLE.getValues());
    }

    public List<Location> findPath(Location start, Location end) {
        if (!start.getWorld().equals(end.getWorld())) {
            return Collections.emptyList();
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Map<Location, Node> allNodes = new HashMap<>();
        Set<Location> closedSet = new HashSet<>();

        Node startNode = new Node(start, null, 0, getDistance(start, end));
        openSet.add(startNode);
        allNodes.put(start, startNode);

        int iterations = 0;

        while (!openSet.isEmpty()) {
            iterations++;
            if (iterations > MAX_PATHFINDING_ITERATIONS) {
                return Collections.emptyList();
            }

            Node currentNode = openSet.poll();
            closedSet.add(currentNode.location);

            if (currentNode.location.getBlockX() == end.getBlockX() &&
                    currentNode.location.getBlockY() == end.getBlockY() &&
                    currentNode.location.getBlockZ() == end.getBlockZ()) {
                return reconstructPath(currentNode);
            }

            for (Location neighborLoc : getNeighbors(currentNode.location)) {
                if (closedSet.contains(neighborLoc)) {
                    continue;
                }

                double newGCost = currentNode.gCost + getDistance(currentNode.location, neighborLoc);
                Node neighborNode = allNodes.get(neighborLoc);

                if (neighborNode == null || newGCost < neighborNode.gCost) {
                    if (neighborNode == null) {
                        neighborNode = new Node(neighborLoc, currentNode, newGCost, getDistance(neighborLoc, end));
                        allNodes.put(neighborLoc, neighborNode);
                    } else {
                        neighborNode.gCost = newGCost;
                        neighborNode.parent = currentNode;
                        neighborNode.fCost = neighborNode.gCost + neighborNode.hCost;
                    }

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unused")
    private boolean isReachable(Location start, Location end) {
        Location playerFeet = start.clone();
        Location playerHead = start.clone().add(0, 1, 0);
        Location targetFeet = end.clone();
        Location targetHead = end.clone().add(0, 1, 0);

        return PASSABLE_MATERIALS.contains(playerFeet.getBlock().getType()) &&
                PASSABLE_MATERIALS.contains(playerHead.getBlock().getType()) &&
                PASSABLE_MATERIALS.contains(targetFeet.getBlock().getType()) &&
                PASSABLE_MATERIALS.contains(targetHead.getBlock().getType());
    }

    private List<Location> reconstructPath(Node endNode) {
        List<Location> path = new ArrayList<>();
        Node currentNode = endNode;
        while (currentNode != null) {
            path.add(currentNode.location);
            currentNode = currentNode.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private double getDistance(Location loc1, Location loc2) {
        return loc1.distance(loc2);
    }

    private List<Location> getNeighbors(Location loc) {
        List<Location> neighbors = new ArrayList<>();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;

                    Location neighborLoc = new Location(loc.getWorld(), x + dx, y + dy, z + dz);
                    if (isWalkable(neighborLoc, loc)) {
                        neighbors.add(neighborLoc);
                    }
                }
            }
        }
        return neighbors;
    }

    private boolean isWalkable(Location loc, Location previousLoc) {
        Block block = loc.getBlock();
        Block blockAbove = loc.clone().add(0, 1, 0).getBlock();
        Block blockBelow = loc.clone().add(0, -1, 0).getBlock();

        if (MaterialSetTag.CLIMBABLE.getValues().contains(block.getType())) {
            return true;
        }

        if (isDiagonalMove(loc, previousLoc)) {
            if (!isDiagonalPassable(previousLoc, loc)) {
                return false;
            }
        }

        if (!PASSABLE_MATERIALS.contains(block.getType()) || !PASSABLE_MATERIALS.contains(blockAbove.getType())) {
            return false;
        }

        if (loc.getBlockY() == previousLoc.getBlockY()) {
            return blockBelow.getType().isSolid() ||
                    blockBelow.getType() == Material.WATER ||
                    blockBelow.getType() == Material.LAVA;
        }

        if (loc.getBlockY() > previousLoc.getBlockY()) {
            int yDiff = loc.getBlockY() - previousLoc.getBlockY();
            if (yDiff != 1) return false;
            return blockBelow.getType().isSolid();
        }

        if (loc.getBlockY() < previousLoc.getBlockY()) {
            int yDiff = previousLoc.getBlockY() - loc.getBlockY();
            if (safeMode) {
                if (yDiff > 3) return false;
                Location landingCheck = loc.clone().add(0, -1, 0);
                return landingCheck.getBlock().getType().isSolid() ||
                        landingCheck.clone().add(0, -1, 0).getBlock().getType().isSolid();
            } else {
                Location checkLoc = loc.clone();
                while (checkLoc.getBlockY() > 0) {
                    checkLoc.add(0, -1, 0);
                    Block checkBlock = checkLoc.getBlock();
                    if (checkBlock.getType().isSolid()) {
                        return true;
                    }
                    if (!PASSABLE_MATERIALS.contains(checkBlock.getType())) {
                        return false;
                    }
                }
                return false;
            }
        }

        return false;
    }

    private boolean isDiagonalMove(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() &&
                from.getBlockZ() != to.getBlockZ();
    }

    private boolean isDiagonalPassable(Location from, Location to) {
        int xDir = Integer.compare(to.getBlockX(), from.getBlockX());
        int zDir = Integer.compare(to.getBlockZ(), from.getBlockZ());

        Location corner1 = new Location(from.getWorld(),
                from.getBlockX() + xDir,
                from.getBlockY(),
                from.getBlockZ());

        Location corner2 = new Location(from.getWorld(),
                from.getBlockX(),
                from.getBlockY(),
                from.getBlockZ() + zDir);

        Block block1 = corner1.getBlock();
        Block block1Above = corner1.clone().add(0, 1, 0).getBlock();
        Block block2 = corner2.getBlock();
        Block block2Above = corner2.clone().add(0, 1, 0).getBlock();

        return (PASSABLE_MATERIALS.contains(block1.getType()) &&
                PASSABLE_MATERIALS.contains(block1Above.getType()) &&
                PASSABLE_MATERIALS.contains(block2.getType()) &&
                PASSABLE_MATERIALS.contains(block2Above.getType()));
    }

    private static class Node {
        Location location;
        Node parent;
        double gCost;
        double hCost;
        double fCost;

        Node(Location location, Node parent, double gCost, double hCost) {
            this.location = location;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}