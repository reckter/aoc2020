package me.reckter.aoc.cords.cube

import me.reckter.aoc.cords.d3.Cord3D

data class Cube(
    val x: Int,
    val y: Int,
    val z: Int
) {
    enum class Direction(val direction: Cube) {
        NorthEast(Cube(1, 0,-1)),
        East(Cube(+1, -1, 0)),
        SouthEast(Cube(0, -1, +1)),
        SouthWest(Cube(-1, 0, +1)),
        West(Cube(-1, +1, 0)),
        NorthWest(Cube(0, +1, -1)),
    }
}



operator fun Cube.plus(other: Cube): Cube {
    return Cube(
        this.x + other.x,
        this.y + other.y,
        this.z + other.z
    )
}





fun Cube.getNeighbors(): List<Cube> {
    return Cube.Direction.values().map { this + it.direction }
}
