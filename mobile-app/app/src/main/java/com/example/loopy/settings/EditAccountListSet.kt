package com.example.loopy.settings

class EditAccountListSet {

    fun setAgeList(): List<Int> {
        val tmp: MutableList<Int> = mutableListOf()
        for (i in 1..100) {
            tmp.add(i)
        }
        return tmp
    }

    fun setHeightList(): List<Int> {
        val tmp: MutableList<Int> = mutableListOf()
        for (i in 1..100) {
            tmp.add(i)
        }
        return tmp
    }

    fun setWeightList(): List<Int> {
        val tmp: MutableList<Int> = mutableListOf()
        for (i in 1..100) {
            tmp.add(i)
        }
        return tmp
    }

}