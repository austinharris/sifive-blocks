// See LICENSE for license details.
package sifive.blocks.ip.xilinx.bufg_gt

import Chisel._

//IP : xilinx unisim BUFG_GT
//Clock Input Buffer
//unparameterized

class BUFG_GT extends BlackBox {
  val io = new Bundle {
    val CE = Bool(INPUT)
    val CEMASK = Bool(INPUT)
    val CLR = Bool(INPUT)
    val CLRMASK = Bool(INPUT)
    val DIV = Bool(INPUT)
    val I = Clock(INPUT)
    val O = Clock(OUTPUT)
  }
}
