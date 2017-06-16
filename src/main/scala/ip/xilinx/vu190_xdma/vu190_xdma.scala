
package sifive.blocks.ip.xilinx.vu190_xdma

import Chisel._
import chisel3.experimental.{Analog,attach}
import config._
import diplomacy._
import uncore.axi4._
import uncore.tilelink2._
import junctions._

trait VU190XDMAIODDR extends Bundle {
  //outputs
  val c0_ddr4_act_n            = Bits(OUTPUT,1)
  val c0_ddr4_adr              = Bits(OUTPUT,17)
  val c0_ddr4_ba               = Bits(OUTPUT,2)
  val c0_ddr4_bg               = Bits(OUTPUT,2)
  val c0_ddr4_cke              = Bits(OUTPUT,2)
  val c0_ddr4_odt              = Bits(OUTPUT,2)
  val c0_ddr4_cs_n             = Bits(OUTPUT,2)
  val c0_ddr4_ck_t             = Bits(OUTPUT,1)
  val c0_ddr4_ck_c             = Bits(OUTPUT,1)
  val c0_ddr4_reset_n          = Bool(OUTPUT)
  val c0_ddr4_par              = Bool(OUTPUT)

  val c0_ddr4_dq               = Analog(72.W)
  val c0_ddr4_dqs_t            = Analog(18.W)
  val c0_ddr4_dqs_c            = Analog(18.W)

  val pcie_7x_mgt_rtl_rxn      = Bits(INPUT,8)
  val pcie_7x_mgt_rtl_rxp      = Bits(INPUT,8)
  val pcie_7x_mgt_rtl_txn      = Bits(OUTPUT,8)
  val pcie_7x_mgt_rtl_txp      = Bits(OUTPUT,8)

  val c0_init_calib_complete   = Bool(OUTPUT)
  val host_done                = Bool(OUTPUT)
}

trait VU190XDMAClocksReset extends Bundle {
  //clock, reset
  val pcie_sys_reset_l      = Bool(INPUT)
  val sys_reset             = Bool(INPUT)
  val s01_aresetn           = Bool(OUTPUT)
  val s01_aclk              = Clock(OUTPUT)

  //differential system clocks
  val c0_sys_clk_n             = Bool(INPUT)
  val c0_sys_clk_p             = Bool(INPUT)
  val pcie_refclk              = Clock(INPUT)
  val pcie_sys_clk_gt          = Clock(INPUT)

  val core_clk                  = Clock(INPUT)
}

class vu190_xdma() extends BlackBox 
{
  val io = new Bundle with VU190XDMAClocksReset
                      with VU190XDMAIODDR {

    //axi_s
    //slave interface write address ports
    val c0_ddr4_s_axi_awid            = Bits(INPUT,4)
    val c0_ddr4_s_axi_awaddr          = Bits(INPUT,35)
    val c0_ddr4_s_axi_awlen           = Bits(INPUT,8)
    val c0_ddr4_s_axi_awsize          = Bits(INPUT,3)
    val c0_ddr4_s_axi_awburst         = Bits(INPUT,2)
    val c0_ddr4_s_axi_awlock          = Bits(INPUT,1)
    val c0_ddr4_s_axi_awcache         = Bits(INPUT,4)
    val c0_ddr4_s_axi_awprot          = Bits(INPUT,3)
    val c0_ddr4_s_axi_awqos           = Bits(INPUT,4)
    val c0_ddr4_s_axi_awvalid         = Bool(INPUT)
    val c0_ddr4_s_axi_awready         = Bool(OUTPUT)
    // val c0_ddr4_s_axi_awregion       = Bits(INPUT,4)
    //slave interface write data ports
    val c0_ddr4_s_axi_wdata           = Bits(INPUT,256)
    val c0_ddr4_s_axi_wstrb           = Bits(INPUT,32)
    val c0_ddr4_s_axi_wlast           = Bool(INPUT)
    val c0_ddr4_s_axi_wvalid          = Bool(INPUT)
    val c0_ddr4_s_axi_wready          = Bool(OUTPUT)
    //slave interface write response ports
    val c0_ddr4_s_axi_bready          = Bool(INPUT)
    val c0_ddr4_s_axi_bid             = Bits(OUTPUT,4)
    val c0_ddr4_s_axi_bresp           = Bits(OUTPUT,2)
    val c0_ddr4_s_axi_bvalid          = Bool(OUTPUT)
    //slave interface read address ports
    val c0_ddr4_s_axi_arid            = Bits(INPUT,4)
    val c0_ddr4_s_axi_araddr          = Bits(INPUT,35)
    val c0_ddr4_s_axi_arlen           = Bits(INPUT,8)
    val c0_ddr4_s_axi_arsize          = Bits(INPUT,3)
    val c0_ddr4_s_axi_arburst         = Bits(INPUT,2)
    val c0_ddr4_s_axi_arlock          = Bits(INPUT,1)
    val c0_ddr4_s_axi_arcache         = Bits(INPUT,4)
    val c0_ddr4_s_axi_arprot          = Bits(INPUT,3)
    val c0_ddr4_s_axi_arqos           = Bits(INPUT,4)
    val c0_ddr4_s_axi_arvalid         = Bool(INPUT)
    val c0_ddr4_s_axi_arready         = Bool(OUTPUT)
    // val c0_ddr4_s_axi_arregion       = Bits(INPUT,4)
    //slave interface read data ports
    val c0_ddr4_s_axi_rready          = Bool(INPUT)
    val c0_ddr4_s_axi_rid             = Bits(OUTPUT,4)
    val c0_ddr4_s_axi_rdata           = Bits(OUTPUT,256)
    val c0_ddr4_s_axi_rresp           = Bits(OUTPUT,2)
    val c0_ddr4_s_axi_rlast           = Bool(OUTPUT)
    val c0_ddr4_s_axi_rvalid          = Bool(OUTPUT) 

    // AXI CTRL port
    val c0_ddr4_s_axi_ctrl_awvalid = Bool(INPUT)
    val c0_ddr4_s_axi_ctrl_awready = Bool(OUTPUT)
    val c0_ddr4_s_axi_ctrl_awaddr = Bits(INPUT,32)
    // Slave Interface Write Data Ports
    val c0_ddr4_s_axi_ctrl_wvalid = Bool(INPUT)
    val c0_ddr4_s_axi_ctrl_wready = Bool(OUTPUT)
    val c0_ddr4_s_axi_ctrl_wdata = Bits(INPUT,32)
    // Slave Interface Write Response Ports
    val c0_ddr4_s_axi_ctrl_bvalid = Bool(OUTPUT)
    val c0_ddr4_s_axi_ctrl_bready = Bool(INPUT)
    val c0_ddr4_s_axi_ctrl_bresp = Bits(OUTPUT,2)
    // Slave Interface Read Address Ports
    val c0_ddr4_s_axi_ctrl_arvalid = Bool(INPUT)
    val c0_ddr4_s_axi_ctrl_arready = Bool(OUTPUT)
    val c0_ddr4_s_axi_ctrl_araddr = Bits(INPUT,32)
    // Slave Interface Read Data Ports
    val c0_ddr4_s_axi_ctrl_rvalid = Bool(OUTPUT)
    val c0_ddr4_s_axi_ctrl_rready = Bool(INPUT)
    val c0_ddr4_s_axi_ctrl_rdata = Bits(OUTPUT,32)
    val c0_ddr4_s_axi_ctrl_rresp = Bits(OUTPUT,2)
  }  
}
