package repro


import io.netty.resolver.AddressResolver
import io.netty.resolver.AddressResolverGroup
import io.netty.resolver.DefaultAddressResolverGroup
import io.netty.util.concurrent.EventExecutor
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Bean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import reactor.netty.http.client.ContextAwareHttpClientMetricsRecorder
import reactor.netty.tcp.TcpClient
import reactor.util.context.ContextView
import java.lang.ref.WeakReference
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@SpringBootTest
@ContextConfiguration(classes = [BaseIntegrationTest.SomeConfig::class])
@DirtiesContext
abstract class BaseIntegrationTest {

    open class SomeConfig {
        @Bean
        open fun metrics() = CustomMetrics()

        @Bean
        open fun resolver() = SpringDependentResolverGroup()

        @Bean
        open fun tcpClient(metrics: CustomMetrics, resolverGroup: SpringDependentResolverGroup): TcpClient {
            val tcpClient = TcpClient.create()
                .resolver(resolverGroup)
                .host("localhost")
                .port(1)
                .metrics(true) { HoldingRecorder(metrics) }
            try {
                tcpClient.connectNow()
            } catch (_: Exception) {}
            return tcpClient
        }

    }

    @Autowired
    protected lateinit var applicationContext: ApplicationContext

    companion object {
        private val contexts = ConcurrentHashMap<Int, WeakReference<ApplicationContext>>()

        @JvmStatic
        protected fun gc() {
            System.gc()
            System.gc()
            Thread.sleep(100)
        }

        @JvmStatic
        protected fun assertNoLeakedContexts(currentContext: ApplicationContext) {
            contexts[System.identityHashCode(currentContext)] = WeakReference(currentContext)
            gc()
            val leaked = contexts.entries
                .filter { it.key != System.identityHashCode(currentContext) }
                .mapNotNull { it.value.get() }
            check(leaked.isEmpty()) {
                "REPRODUCED: closed ApplicationContexts are held by a strong reference after GC: ${leaked.size} item(s) " +
                        "(this is exactly the memory leak between test classes)"
            }
        }
    }

    @BeforeEach
    fun checkForLeaks() {
        assertNoLeakedContexts(applicationContext)
    }
}

class HoldingRecorder(val metrics: CustomMetrics): ContextAwareHttpClientMetricsRecorder() {
    override fun recordDataReceivedTime(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String,
        method: String,
        status: String,
        time: Duration
    ) {
        
    }

    override fun recordDataSentTime(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String,
        method: String,
        time: Duration
    ) {
        
    }

    override fun recordResponseTime(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String,
        method: String,
        status: String,
        time: Duration
    ) {
        
    }

    override fun incrementErrorsCount(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String
    ) {
        
    }

    override fun recordDataReceived(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String,
        bytes: Long
    ) {
        
    }

    override fun recordDataSent(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        uri: String,
        bytes: Long
    ) {
        
    }

    override fun incrementErrorsCount(
        contextView: ContextView,
        remoteAddress: SocketAddress
    ) {
        
    }

    override fun recordConnectTime(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        time: Duration,
        status: String
    ) {
        
    }

    override fun recordDataReceived(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        bytes: Long
    ) {
        
    }

    override fun recordDataSent(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        bytes: Long
    ) {
        
    }

    override fun recordTlsHandshakeTime(
        contextView: ContextView,
        remoteAddress: SocketAddress,
        time: Duration,
        status: String
    ) {
        
    }

    override fun recordResolveAddressTime(
        remoteAddress: SocketAddress,
        time: Duration,
        status: String
    ) {
    }
}

class CustomMetrics : ApplicationContextAware {
    @Suppress("PLATFORM_CLASS_MISMATCH_JVM")
    lateinit var applicationContext: ApplicationContext
        private set

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
}

class SpringDependentResolverGroup:
    AddressResolverGroup<InetSocketAddress>() {

    private val delegate: AddressResolverGroup<InetSocketAddress> =
        DefaultAddressResolverGroup.INSTANCE

    override fun newResolver(executor: EventExecutor): AddressResolver<InetSocketAddress> =
        delegate.getResolver(executor)
}
