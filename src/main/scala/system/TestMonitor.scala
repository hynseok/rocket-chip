package freechips.rocketchip.system

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.tile._
import freechips.rocketchip.subsystem.{BaseSubsystem, PBUS}

class InstMonitorBundle extends Bundle {
  val data  = UInt(32.W)
  val valid = Bool()
}

class TestMonitor(busWidth: Int, addr: Int) (implicit p: Parameters) extends LazyModule {
  val device = new SimpleDevice("test-monitor", Seq("hynseok, test-monitor"))

  val node = TLRegisterNode(
    address = Seq(AddressSet(addr, 0xfff)),
    device = device,
    beatBytes = busWidth
  )

  val monitorNode = BundleBridgeSink[InstMonitorBundle]()

  override lazy val module = new TestMonitorModuleImp(this)
}

class TestMonitorModuleImp(outer: TestMonitor) extends LazyModuleImp(outer) {
  val monitor_in = outer.monitorNode.bundle

  val fifo = Module(new Queue(UInt(32.W), 1024))
  fifo.io.enq.bits  := monitor_in.data
  fifo.io.enq.valid := monitor_in.valid
    
  outer.node.regmap(
    0x00 -> Seq(RegField.r(32, fifo.io.deq)), 
    0x08 -> Seq(RegField.r(1, !fifo.io.deq.valid))
  )
}

trait CanHavePeripheryTestMonitor { this: freechips.rocketchip.subsystem.RocketSubsystem =>
  val monitorAddr = 0x20000
  val pbus = locateTLBusWrapper(PBUS)
  
  val testMonitor = pbus { LazyModule(new TestMonitor(pbus.beatBytes, monitorAddr)) }
  
  pbus.coupleTo("test-monitor") {
    testMonitor.node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _
  }

  rocketTiles.headOption.foreach {
    case rTile: RocketTile =>
      testMonitor.monitorNode := rTile.monitorNode
    case _ =>
  }
}
