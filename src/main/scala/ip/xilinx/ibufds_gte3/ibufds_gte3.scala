// See LICENSE for license details.
package sifive.blocks.ip.xilinx.ibufds_gte3

import Chisel._

//IP : xilinx unisim IBUFDS_GTE2
//Differential Signaling Input Buffer
//unparameterized

class IBUFDS_GTE3 extends BlackBox {
  val io = new Bundle {
    val O         = Clock(OUTPUT)
    val ODIV2     = Clock(OUTPUT)
    val CEB       = Bool(INPUT)
    val I         = Bool(INPUT)
    val IB        = Bool(INPUT)
  }
}