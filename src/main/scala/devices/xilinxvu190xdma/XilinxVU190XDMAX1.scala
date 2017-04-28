package sifive.blocks.devices.xilinxvu190xdma

import Chisel._
import chisel3.experimental.{Analog,attach}
import config._
import diplomacy._
import uncore.tilelink2._
import uncore.axi4._
import rocketchip._
import sifive.blocks.ip.xilinx.vu190_xdma.{VU190XDMAClocksReset, VU190XDMAIODDR, vu190_xdma}
import sifive.blocks.ip.xilinx.ibufds_gte3.IBUFDS_GTE3
import sifive.blocks.ip.xilinx.bufg_gt.BUFG_GT

class XilinxVU190XDMAPads extends Bundle with VU190XDMAIODDR

class XilinxVU190XDMAIO extends Bundle with VU190XDMAIODDR with VU190XDMAClocksReset {
  val pcie_sys_clk_clk_p = Bool(INPUT)
  val pcie_sys_clk_clk_n = Bool(INPUT)
  val safe_aresetn       = Bool(INPUT)
}

class XilinxVU190XDMA(implicit p: Parameters) extends LazyModule {
  val device = new MemoryDevice
  val node = TLInputNode()
  val axi4 = AXI4InternalOutputNode(Seq(AXI4SlavePortParameters(
                                          slaves = Seq(AXI4SlaveParameters(
                                                         address = Seq(AddressSet(p(ExtMem).base, p(ExtMem).size-1)),
                                                         resources     = device.reg,
                                                         regionType    = RegionType.UNCACHED,
                                                         executable    = true,
                                                         supportsWrite = TransferSizes(1, 256*8),
                                                         supportsRead  = TransferSizes(1, 256*8),
                                                         interleavedId = Some(0))),
                                          beatBytes = 8)))

  //val xing = LazyModule(new TLAsyncCrossing)
  val toaxi4 = LazyModule(new TLToAXI4(idBits = 4))

  //xing.node := node
  val monitor = (toaxi4.node := node)
  axi4 := toaxi4.node

   lazy val module = new LazyModuleImp(this) {
    val io = new Bundle {
      val port = new XilinxVU190XDMAIO
      val tl = node.bundleIn
    }

    //DMA black box instantiation
    val blackbox = Module(new vu190_xdma) 
    //PCIe Reference Clock
    val ibufds_gte3 = Module(new IBUFDS_GTE3)
    blackbox.io.pcie_refclk := ibufds_gte3.io.ODIV2
    blackbox.io.pcie_sys_clk_gt := ibufds_gte3.io.O
    ibufds_gte3.io.CEB := UInt(0)
    ibufds_gte3.io.I := io.port.pcie_sys_clk_clk_p
    ibufds_gte3.io.IB := io.port.pcie_sys_clk_clk_n

    //Generate 50MHz clock from the 100MHz pcie ref clk
    // val bufg_gt = Module(new BUFG_GT)
    // bufg_gt.io.CE := Bool(true)
    // bufg_gt.io.CEMASK := Bool(false)
    // bufg_gt.io.CLRMASK := Bool(false)
    // bufg_gt.io.DIV := UInt(1)
    // bufg_gt.io.I := ibufds_gte3.io.ODIV2
    // bufg_gt.io.CLR := Bool(false)
    // io.port.sys_clk_50 := bufg_gt.io.O

    //pins to top level
    io.port.c0_init_calib_complete := blackbox.io.c0_init_calib_complete
    io.port.host_done := blackbox.io.host_done

    //inouts
    attach(io.port.c0_ddr4_dq, blackbox.io.c0_ddr4_dq)
    attach(io.port.c0_ddr4_dqs_t, blackbox.io.c0_ddr4_dqs_t)
    attach(io.port.c0_ddr4_dqs_c, blackbox.io.c0_ddr4_dqs_c)

    //outputs
    io.port.c0_ddr4_act_n        := blackbox.io.c0_ddr4_act_n
    io.port.c0_ddr4_adr          := blackbox.io.c0_ddr4_adr
    io.port.c0_ddr4_ba           := blackbox.io.c0_ddr4_ba
    io.port.c0_ddr4_bg           := blackbox.io.c0_ddr4_bg
    io.port.c0_ddr4_cke          := blackbox.io.c0_ddr4_cke
    io.port.c0_ddr4_odt          := blackbox.io.c0_ddr4_odt
    io.port.c0_ddr4_cs_n         := blackbox.io.c0_ddr4_cs_n
    io.port.c0_ddr4_ck_t         := blackbox.io.c0_ddr4_ck_t
    io.port.c0_ddr4_ck_c         := blackbox.io.c0_ddr4_ck_c
    io.port.c0_ddr4_reset_n      := blackbox.io.c0_ddr4_reset_n
    io.port.c0_ddr4_par          := blackbox.io.c0_ddr4_par

    //inputs
    //differential system clock
    blackbox.io.c0_sys_clk_n     := io.port.c0_sys_clk_n
    blackbox.io.c0_sys_clk_p     := io.port.c0_sys_clk_p
    // blackbox.io.pcie_refclk      := io.port.pcie_refclk
    // blackbox.io.pcie_sys_clk_gt  := io.port.pcie_sys_clk_gt

    //user interface signals
    val axi_async = axi4.bundleIn(0)
    //xing.module.io.in_clock := clock
    //xing.module.io.in_reset := reset
    //xing.module.io.out_clock := blackbox.io.s01_aclk
    // xing.module.io.out_reset := blackbox.io.s01_aresetn
   // xing.module.io.out_reset := io.port.safe_aresetn
    toaxi4.module.clock := blackbox.io.s01_aclk
    // toaxi4.module.reset := blackbox.io.s01_aresetn
    toaxi4.module.reset := io.port.safe_aresetn 
     monitor.foreach { lm =>
       lm.module.clock := blackbox.io.s01_aclk
       // lm.module.reset := blackbox.io.s01_aresetn
       lm.module.reset := blackbox.io.s01_aresetn
     }
    //xing.module.io.out_clock := blackbox.io.c0_ddr4_ui_clk
    //xing.module.io.out_reset := blackbox.io.c0_ddr4_ui_clk_sync_rst
    //toaxi4.module.clock := blackbox.io.c0_ddr4_ui_clk
    //toaxi4.module.reset := blackbox.io.c0_ddr4_ui_clk_sync_rst

    //blackbox.io.c0_ddr4_aresetn       := io.port.c0_ddr4_aresetn

    //slave AXI interface write address ports
    blackbox.io.c0_ddr4_s_axi_awid    := axi_async.aw.bits.id
    blackbox.io.c0_ddr4_s_axi_awaddr  := Cat(UInt("b0000"), axi_async.aw.bits.addr) //truncation ??
    blackbox.io.c0_ddr4_s_axi_awlen   := axi_async.aw.bits.len
    blackbox.io.c0_ddr4_s_axi_awsize  := axi_async.aw.bits.size
    blackbox.io.c0_ddr4_s_axi_awburst := axi_async.aw.bits.burst
    blackbox.io.c0_ddr4_s_axi_awlock  := axi_async.aw.bits.lock
    blackbox.io.c0_ddr4_s_axi_awcache := UInt("b0011")
    blackbox.io.c0_ddr4_s_axi_awprot  := axi_async.aw.bits.prot
    blackbox.io.c0_ddr4_s_axi_awqos   := axi_async.aw.bits.qos
    blackbox.io.c0_ddr4_s_axi_awvalid := axi_async.aw.valid
    axi_async.aw.ready        := blackbox.io.c0_ddr4_s_axi_awready

    //slave interface write data ports
    blackbox.io.c0_ddr4_s_axi_wdata   := axi_async.w.bits.data
    blackbox.io.c0_ddr4_s_axi_wstrb   := axi_async.w.bits.strb
    blackbox.io.c0_ddr4_s_axi_wlast   := axi_async.w.bits.last
    blackbox.io.c0_ddr4_s_axi_wvalid  := axi_async.w.valid
    axi_async.w.ready         := blackbox.io.c0_ddr4_s_axi_wready

    //slave interface write response
    blackbox.io.c0_ddr4_s_axi_bready  := axi_async.b.ready
    axi_async.b.bits.id       := blackbox.io.c0_ddr4_s_axi_bid
    axi_async.b.bits.resp     := blackbox.io.c0_ddr4_s_axi_bresp
    axi_async.b.valid         := blackbox.io.c0_ddr4_s_axi_bvalid

    //slave AXI interface read address ports
    blackbox.io.c0_ddr4_s_axi_arid    := axi_async.ar.bits.id
    blackbox.io.c0_ddr4_s_axi_araddr  := Cat(UInt("b0000"), axi_async.ar.bits.addr) //truncation ??
    blackbox.io.c0_ddr4_s_axi_arlen   := axi_async.ar.bits.len
    blackbox.io.c0_ddr4_s_axi_arsize  := axi_async.ar.bits.size
    blackbox.io.c0_ddr4_s_axi_arburst := axi_async.ar.bits.burst
    blackbox.io.c0_ddr4_s_axi_arlock  := axi_async.ar.bits.lock
    blackbox.io.c0_ddr4_s_axi_arcache := UInt("b0011")
    blackbox.io.c0_ddr4_s_axi_arprot  := axi_async.ar.bits.prot
    blackbox.io.c0_ddr4_s_axi_arqos   := axi_async.ar.bits.qos
    blackbox.io.c0_ddr4_s_axi_arvalid := axi_async.ar.valid
    axi_async.ar.ready        := blackbox.io.c0_ddr4_s_axi_arready

    //slace AXI interface read data ports
    blackbox.io.c0_ddr4_s_axi_rready  := axi_async.r.ready
    axi_async.r.bits.id       := blackbox.io.c0_ddr4_s_axi_rid
    axi_async.r.bits.data     := blackbox.io.c0_ddr4_s_axi_rdata
    axi_async.r.bits.resp     := blackbox.io.c0_ddr4_s_axi_rresp
    axi_async.r.bits.last     := blackbox.io.c0_ddr4_s_axi_rlast
    axi_async.r.valid         := blackbox.io.c0_ddr4_s_axi_rvalid

    //misc
    blackbox.io.sys_reset             :=io.port.sys_reset
    blackbox.io.pcie_sys_reset_l       :=io.port.pcie_sys_reset_l
    //io.port.div2_clk                  :=blackbox.io.sys_clk_50
    io.port.s01_aresetn := blackbox.io.s01_aresetn
    io.port.s01_aclk := blackbox.io.s01_aclk

    blackbox.io.pcie_7x_mgt_rtl_rxn := io.port.pcie_7x_mgt_rtl_rxn
    blackbox.io.pcie_7x_mgt_rtl_rxp := io.port.pcie_7x_mgt_rtl_rxp
    io.port.pcie_7x_mgt_rtl_txn := blackbox.io.pcie_7x_mgt_rtl_txn
    io.port.pcie_7x_mgt_rtl_txp := blackbox.io.pcie_7x_mgt_rtl_txp

    //ctl
    //axi-lite slave interface write address
    blackbox.io.c0_ddr4_s_axi_ctrl_awaddr    := UInt(0)
    blackbox.io.c0_ddr4_s_axi_ctrl_awvalid   := Bool(false)
    // c.aw.ready                 := blackbox.io.c0_ddr4_s_axi_ctrl_awready
    //axi-lite slave interface write data ports
    blackbox.io.c0_ddr4_s_axi_ctrl_wdata     := UInt(0)
    blackbox.io.c0_ddr4_s_axi_ctrl_wvalid    := Bool(false)
    // c.w.ready                  := blackbox.io.c0_ddr4_s_axi_ctrl_wready
    //axi-lite slave interface write response
    blackbox.io.c0_ddr4_s_axi_ctrl_bready    := Bool(false)
    // c.b.bits.id                := UInt(0)
    // c.b.bits.resp              := blackbox.io.c0_ddr4_s_axi_ctrl_bresp
    // c.b.valid                  := blackbox.io.c0_ddr4_s_axi_ctrl_bvalid
    //axi-lite slave AXI interface read address ports
    blackbox.io.c0_ddr4_s_axi_ctrl_araddr    := UInt(0)
    blackbox.io.c0_ddr4_s_axi_ctrl_arvalid   := Bool(false)
    // c.ar.ready                 := blackbox.io.c0_ddr4_s_axi_ctrl_arready
    //slave AXI interface read data ports
    blackbox.io.c0_ddr4_s_axi_ctrl_rready    := Bool(false)
  }
}
