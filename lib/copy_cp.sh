#!/bin/bash


CLASSPATH="/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/classes/java/main:/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/resources/main:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.code.findbugs/jsr305/3.0.2/25ea2e8b0c338a877313bd4672d3fe056ea78f0d/jsr305-3.0.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.errorprone/error_prone_annotations/2.3.1/a6a2b2df72fd13ec466216049b303f206bd66c5d/error_prone_annotations-2.3.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.guava/guava/25.1-jre/6c57e4b22b44e89e548b5c9f70f0c45fe10fb0b4/guava-25.1-jre.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.squareup/javapoet/1.11.1/210e69f58dfa76c5529a303913b4a30c2bfeb76b/javapoet-1.11.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.7/557edd918fd41f9260963583ebf5a61a43a6b423/commons-lang3-3.7.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.checkerframework/checker-qual/2.0.0/518929596ee3249127502a8573b2e008e2d51ed3/checker-qual-2.0.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.errorprone/error_prone_annotations/2.1.3/39b109f2cd352b2d71b52a3b5a1a9850e1dc304b/error_prone_annotations-2.1.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.j2objc/j2objc-annotations/1.1/ed28ded51a8b1c6b112568def5f4b455e6809019/j2objc-annotations-1.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.codehaus.mojo/animal-sniffer-annotations/1.14/775b7e22fb10026eed3f86e8dc556dfafe35f2d5/animal-sniffer-annotations-1.14.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.jakewharton.fliptables/fliptables/1.0.2/797b709114c107947e4796bdb506ab0e62eaf5ce/fliptables-1.0.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.github.jbellis/jamm/0.3.2/9f23a1b5184d87099b7ed06c15d2fd27ea966ab7/jamm-0.3.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.trivago/triava/1.0.5/63ab9853aff5bb9bcfb2af9eb0d41345250f8640/triava-1.0.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.cache2k/cache2k-core/1.0.2.Final/c49bd58478f307c1b2960149e7874609c5396724/cache2k-core-1.0.2.Final.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ehcache/ehcache/3.5.2/87f3b9316e8d93221a9184fc429af78ade183508/ehcache-3.5.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.openhft/koloboke-impl-jdk8/0.6.8/b367ae752f987f0eb3178c4340ec2dad74209230/koloboke-impl-jdk8-0.6.8.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.openhft/koloboke-api-jdk8/0.6.8/461925316a141df2197d0e5c3f6f9942267fa4ea/koloboke-api-jdk8-0.6.8.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.rapidoid/rapidoid-commons/5.5.4/249b8ee104e2b309050ff00482a5ccc596965a3/rapidoid-commons-5.5.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-nop/1.7.25/8c7708c79afec923de8957b7d4f90177628b9fcd/slf4j-nop-1.7.25.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/systems.comodal/collision/0.3.3/b016e4327fbb167f8cc2b3709893af3655ee0989/collision-0.3.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-core/1.9.2/b9ed6a51a3cdf76926c1a068aeb89f4d0aea2d87/oak-core-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.jodah/expiringmap/0.5.8/48c79672c74c5605042a3343e9d8a07ad8372be2/expiringmap-0.5.8.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.elasticsearch/elasticsearch/6.2.4/6d8457abdfc9fe0eb6b7ba99f00605c297e433fd/elasticsearch-6.2.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.googlecode.concurrentlinkedhashmap/concurrentlinkedhashmap-lru/1.4.2/2eaf3d3c9746d526ff7e5b93931d482c3887e6ac/concurrentlinkedhashmap-lru-1.4.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.inject/guice/4.2.0/25e1f4c1d528a1cffabcca0d432f634f3132f6c8/guice-4.2.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.google.guava/guava-testlib/25.1-jre/c4358f1ca5398b199ced023c00440840ca513b6e/guava-testlib-25.1-jre.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-security-spi/1.9.2/4597e03e8d349f7ffdb182cbab65ea71365f59a5/oak-security-spi-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-query-spi/1.9.2/c2e3ca4897e56a8b12690bbcad656792fb46426f/oak-query-spi-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-blob-plugins/1.9.2/8475d8d00e58a4b4294b21c160c51fab5ef2434a/oak-blob-plugins-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-store-spi/1.9.2/4d081992a07b3d6ae0bcb834b81bc835dad8dc93/oak-store-spi-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-core-spi/1.9.2/7af811332bd79d47b7e5c9204725d0d80df91b97/oak-core-spi-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-blob/1.9.2/bca5614c22f1f508b80bb79e8eb2f8b94c73d950/oak-blob-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-commons/1.9.2/92b3af06dfa06c9742385b948695487b129e2e9c/oak-commons-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.mockito/mockito-core/2.18.3/98aa130476c5d1915dac35b5ad053a7ffcd675bc/mockito-core-2.18.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.hamcrest/java-hamcrest/2.0.0.0/f1c8853ade0ecf707f5a261c830e98893983813/java-hamcrest-2.0.0.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.awaitility/awaitility/3.1.0/b5e6a9bb43b175cb53f43d56257316c4916c5e11/awaitility-3.1.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.felix/org.apache.felix.framework/5.6.10/132190671f2597f47362554304bec2794e9179b8/org.apache.felix.framework-5.6.10.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.exam/pax-exam-junit4/4.11.0/aa5da7072b91106944a6dd65a2539ffa981f7268/pax-exam-junit4-4.11.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.github.brianfrankcooper.ycsb/core/0.13.0/f8ee33ea923766c6508eb1553cbb12e9abe36c25/core-0.13.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/it.unimi.dsi/fastutil/8.1.1/5f0cc29ae9c2ea9a04db247abf2444cd6f14e0b4/fastutil-8.1.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/junit/junit/4.12/2973d150c0dc1fefe998f834810d68f278ea58ec/junit-4.12.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.testng/testng/6.14.3/d24515dc253e77e54b73df97e1fb2eb7faf34fdd/testng-6.14.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.jctools/jctools-core/2.1.2/8ec46a6a26e7c1c7e57e2590a043238ffc462144/jctools-core-2.1.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.openjdk.jmh/jmh-generator-bytecode/1.21/6a52cbbd7f5e2cf7a0163984241750cdd6cb1257/jmh-generator-bytecode-1.21.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.openjdk.jmh/jmh-generator-asm/1.21/670ffd88255faf81ad294f0d8a9eba0292f5a554/jmh-generator-asm-1.21.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.openjdk.jmh/jmh-generator-reflection/1.21/ed5a2bdca04daafac41c53cf82c3b9733fd91e89/jmh-generator-reflection-1.21.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.openjdk.jmh/jmh-core/1.21/442447101f63074c61063858033fbfde8a076873/jmh-core-1.21.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/javax.cache/cache-api/1.0.0/2b57384801243f387f1a2e7ab8066ac79c2a91d3/cache-api-1.0.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.cache2k/cache2k-api/1.0.2.Final/7ae82693c027901af95cf0186bc2cd61f70eb666/cache2k-api-1.0.2.Final.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.exam/pax-exam-spi/4.11.0/7e1465c42e8c4d24824b36201fcac773e3b8dd1b/pax-exam-spi-4.11.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/jackrabbit-data/2.17.3/91f934877ed7ff5bd9e6f2e7795c4dc3e16e8c58/jackrabbit-data-2.17.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.slf4j/jcl-over-slf4j/1.7.25/f8c32b13ff142a513eeb5b6330b1588dcb2c0461/jcl-over-slf4j-1.7.25.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.exam/pax-exam/4.11.0/ddec1f74427f8acaab5baad3d7c37983d988485d/pax-exam-4.11.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.tinybundles/tinybundles/2.1.1/d894c29d13f0d7a9094793c25a0a7723b9537c0b/tinybundles-2.1.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-store/1.5.0/7c5d6ed88638a61b15b3c285b8c16eee7753de1c/ops4j-base-store-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.25/da76ca59f6a57ee3102f8f9bd9cee742973efa8a/slf4j-api-1.7.25.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.rapidoid/rapidoid-essentials/5.5.4/b313429fa8d8489f2359a72fbb93a7291da9b07b/rapidoid-essentials-5.5.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.javassist/javassist/3.22.0-GA/3e83394258ae2089be7219b971ec21a8288528ad/javassist-3.22.0-GA.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.module/jackson-module-afterburner/2.8.5/b5f9e0e1d850569147110e8ed62947957f5ee3e8/jackson-module-afterburner-2.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.dataformat/jackson-dataformat-xml/2.8.5/e6f39a5929c3dc8e5506fd1f8886f819a14dfbdf/jackson-dataformat-xml-2.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.module/jackson-module-jaxb-annotations/2.8.5/11ba9aba1c9dd9d94b92ecfc10cfffac2f1753bb/jackson-module-jaxb-annotations-2.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-databind/2.8.5/b3035f37e674c04dafe36a660c3815cc59f764e2/jackson-databind-2.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml/2.8.10/1e08caf1d787c825307d8cc6362452086020d853/jackson-dataformat-yaml-2.8.10.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/javax.inject/javax.inject/1/6975da39a7040257bd51d21a231b76c915872d38/javax.inject-1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.osgi/org.osgi.service.component.annotations/1.3.0/ef17ec0e7f73cd144599da108d72a24285d1f5d5/org.osgi.service.component.annotations-1.3.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.osgi/org.osgi.service.metatype.annotations/1.3.0/793a335fb4d18190a2e7a89614001c65853c91c5/org.osgi.service.metatype.annotations-1.3.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/oak-api/1.9.2/3cd747eccebd93f5361a3eac56f70ddefac964c0/oak-api-1.9.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/commons-io/commons-io/2.6/815893df5f31da2ece4040fe0a12fd44b577afaf/commons-io-2.6.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/javax.jcr/jcr/2.0/8297216bcfe4aea369ed6ee0d1718133f752e97/jcr-2.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/jackrabbit-api/2.17.3/802400ee8af97ae0de51f67e8b9fb166599e8444/jackrabbit-api-2.17.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.jackrabbit/jackrabbit-jcr-commons/2.17.3/d880cbe1517247563a9b83d1f3790f61ea79b03c/jackrabbit-jcr-commons-2.17.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.elasticsearch/elasticsearch-core/6.2.4/99611d82dba516f0e48a6d65a788e5df4a6219eb/elasticsearch-core-6.2.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.elasticsearch/securesm/1.2/4c28f5b634497d64b727961430a516f351a099d5/securesm-1.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.elasticsearch/elasticsearch-cli/6.2.4/73b92527f0bc0802cf74c3fb209d4f8b43258509/elasticsearch-cli-6.2.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.carrotsearch/hppc/0.7.1/8b5057f74ea378c0150a1860874a3ebdcb713767/hppc-0.7.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/joda-time/joda-time/2.9.9/f7b520c458572890807d143670c9b24f4de90897/joda-time-2.9.9.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.yaml/snakeyaml/1.17/7a27ea250c5130b2922b86dea63cbb1cc10a660c/snakeyaml-1.17.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-core/2.8.10/eb21a035c66ad307e66ec8fce37f5d50fd62d039/jackson-core-2.8.10.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.dataformat/jackson-dataformat-smile/2.8.10/e853081fadaad3e98ed801937acc3d8f77580686/jackson-dataformat-smile-2.8.10.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.dataformat/jackson-dataformat-cbor/2.8.10/1c58cc9313ddf19f0900cd61ed044874278ce320/jackson-dataformat-cbor-2.8.10.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.tdunning/t-digest/3.0/84ccf145ac2215e6bfa63baa3101c0af41017cfc/t-digest-3.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.hdrhistogram/HdrHistogram/2.1.9/e4631ce165eb400edecfa32e03d3f1be53dee754/HdrHistogram-2.1.9.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.logging.log4j/log4j-api/2.9.1/7a2999229464e7a324aa503c0a52ec0f05efe7bd/log4j-api-2.9.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.elasticsearch/jna/4.5.1/da10908ae23dc59b19dc258e63aea1c44621dc3a/jna-4.5.1.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy/1.8.5/f16b6f8bf487d06e9f83da3033958a694f57c8a4/byte-buddy-1.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.bytebuddy/byte-buddy-agent/1.8.5/7bb88bffec91556155629ad4ff6a0e0013d4bf10/byte-buddy-agent-1.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.objenesis/objenesis/2.6/639033469776fd37c08358c6b92a4761feb2af4b/objenesis-2.6.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-io/1.5.0/15acc9a1b56c8963db471cee926d7001591e6b4d/ops4j-base-io-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-lang/1.5.0/da31d176ffa8b78c0b83e183951c86cbd7bfb0b9/ops4j-base-lang-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.htrace/htrace-core4/4.1.0-incubating/12b3e2adda95e8c41d9d45d33db075137871d2e2/htrace-core4-4.1.0-incubating.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.codehaus.jackson/jackson-mapper-asl/1.9.4/5206191b35112f50b8e25fcbd3f3b84e12e11cee/jackson-mapper-asl-1.9.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.codehaus.jackson/jackson-core-asl/1.9.4/8d8b2a3e5bc77ee1be67d060b44ac77d48a27d6e/jackson-core-asl-1.9.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.hamcrest/hamcrest-core/1.3/42a25dc3219429f0e5d060061f71acb49bf010a0/hamcrest-core-1.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.beust/jcommander/1.72/6375e521c1e11d6563d4f25a07ce124ccf8cd171/jcommander-1.72.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache-extras.beanshell/bsh/2.0b6/fb418f9b33a0b951e9a2978b4b6ee93b2707e72f/bsh-2.0b6.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/aopalliance/aopalliance/1.0/235ba8b489512805ac13a8f9ea77a1ca5ebe3e8/aopalliance-1.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/net.sf.jopt-simple/jopt-simple/5.0.2/98cafc6081d5632b61be2c9e60650b64ddbc637c/jopt-simple-5.0.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.apache.commons/commons-math3/3.2/ec2544ab27e110d2d431bdad7d538ed509b21e62/commons-math3-3.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.jackson.core/jackson-annotations/2.8.5/9d82ff47bc2c2d3b5b6a6618fe636782bbcd5b07/jackson-annotations-2.8.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/com.fasterxml.woodstox/woodstox-core/5.0.3/10aa199207fda142eff01cd61c69244877d71770/woodstox-core-5.0.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.codehaus.woodstox/stax2-api/3.1.4/ac19014b1e6a7c08aad07fe114af792676b685b7/stax2-api-3.1.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/commons-codec/commons-codec/1.11/3acb4705652e16236558f0f4f2192cc33c3bd189/commons-codec-1.11.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-spi/1.5.0/59c602ebd584b7326f75c76983174c9f4583e36b/ops4j-base-spi-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/5.0.3/dcc2193db20e19e1feca8b1240dbbc4e190824fa/asm-5.0.3.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-util-property/1.5.0/10a2f7cfa055e776eb996ca456747a07fdf2015e/ops4j-base-util-property-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/biz.aQute.bnd/bndlib/2.4.0/de13867e8e5d1f9d6b5ab5cbb16b6cfdbffdc6d2/bndlib-2.4.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-monitors/1.5.0/5057dad1ed213c16d5320a11d955523020de73f3/ops4j-base-monitors-1.5.0.jar:/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/classes/java/test:/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/resources/test:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.hdrhistogram/HdrHistogram/2.1.4/2b0da22320f0ac0bbedc7b9df7881d9853d8a754/HdrHistogram-2.1.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.6.6/ce53b0a0e2cfbb27e8a59d38f79a18a5c6a8d2b0/slf4j-api-1.6.6.jar:/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/classes/java/jmh:/Users/erfanarvan/Desktop/comparisonPaper/experiments/organized/projects/caffeine_CFNullness/pre_wpi_caffeine_CFNullness/caffeine/build/resources/jmh:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.exam/pax-exam-container-native/4.11.0/b8040dee48145f6603703489995e3c9498df33f0/pax-exam-container-native-4.11.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.exam/pax-exam-link-mvn/4.11.0/136b73ba0594040d7c2d971a7cfe1752ab5ea776/pax-exam-link-mvn-4.11.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.url/pax-url-aether/2.5.4/2d3d0820ebf12adedbefd33cd46ddcc33efdfafb/pax-url-aether-2.5.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.swissbox/pax-swissbox-core/1.8.2/5c5b2b5df3a59826a55af9c6ca1b6d227052dbdd/pax-swissbox-core-1.8.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.swissbox/pax-swissbox-tracker/1.8.2/699d52d350f2377b30a1927a52c4b43ca4d8e5c5/pax-swissbox-tracker-1.8.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.base/ops4j-base-net/1.5.0/4a124538e1c3fe590f502602ece85612c31c8e88/ops4j-base-net-1.5.0.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.url/pax-url-link/2.4.5/eb9065c74a008e641389366748dba5cc6630ce8c/pax-url-link-2.4.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.url/pax-url-classpath/2.4.5/fa99960fad95b2f8cf64c380a74bf845d9084a27/pax-url-classpath-2.4.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.url/pax-url-commons/2.4.5/342030b66367f84c82ca5b82cb7e230660156766/pax-url-commons-2.4.5.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.swissbox/pax-swissbox-property/1.8.2/27ef3c4c34771b0455b2036bb93b36c74a729306/pax-swissbox-property-1.8.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.url/pax-url-aether-support/2.5.4/6c4ac88ca164b56a03b12fa8cecbde61531ad7c9/pax-url-aether-support-2.5.4.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.slf4j/jcl-over-slf4j/1.6.6/ec497945fdcaf7fd970ae9931b9bbfaf735d385e/jcl-over-slf4j-1.6.6.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.swissbox/pax-swissbox-lifecycle/1.8.2/3d5fb3a2f861579b29cd64c5d56b1c4b90ec93e0/pax-swissbox-lifecycle-1.8.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.ops4j.pax.swissbox/pax-swissbox-optional-jcl/1.8.2/17c32e1446bbf5561c972db9786fdf880f3d1f64/pax-swissbox-optional-jcl-1.8.2.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.eclipse.aether/aether-impl/1.0.2.v20150114/f147539e6e60dfbda9ef7f6d750066170f61b7a1/aether-impl-1.0.2.v20150114.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.eclipse.aether/aether-spi/1.0.2.v20150114/8428dfa330107984f3e3ac05cc3ebd50b2676866/aether-spi-1.0.2.v20150114.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.eclipse.aether/aether-util/1.0.2.v20150114/d2d3c74a5210544b5cdce89a2c1d1c62835692d1/aether-util-1.0.2.v20150114.jar:/Users/erfanarvan/.gradle/caches/modules-2/files-2.1/org.eclipse.aether/aether-api/1.0.2.v20150114/839f93a5213fb3e233b09bfd6d6b95669f7043c0/aether-api-1.0.2.v20150114.jar"

# Define the target directory as the current directory
TARGET_DIR="."

# Split the classpath into an array
IFS=':' read -r -a paths <<< "$CLASSPATH"

# Create a temporary directory for extracting AAR files
TEMP_DIR=$(mktemp -d)

# Function to extract JAR files from AAR files and copy to the target directory
extract_jar_from_aar() {
    aar_file=$1
    aar_name=$(basename "$aar_file" .aar)
    dest_dir="$TARGET_DIR"

    unzip -o "$aar_file" -d "$TEMP_DIR"
    
    if [ -f "$TEMP_DIR/classes.jar" ]; then
        echo "Copying classes.jar from $aar_file to $dest_dir/$aar_name.jar"
        mv "$TEMP_DIR/classes.jar" "$dest_dir/$aar_name.jar"
    else
        echo "classes.jar not found in $aar_file"
    fi
    
    if [ -d "$TEMP_DIR/libs" ]; then
        for jar in "$TEMP_DIR/libs/"*.jar; do
            if [ -f "$jar" ]; then
                jar_name=$(basename "$jar")
                echo "Copying $jar from $aar_file to $dest_dir/$aar_name-$jar_name"
                mv "$jar" "$dest_dir/$aar_name-$jar_name"
            else
                echo "No JAR files found in libs directory of $aar_file"
            fi
        done
    else
        echo "libs directory not found in $aar_file"
    fi
    
    rm -rf "$TEMP_DIR/*"
}

# Copy each item in the classpath to the target directory
for path in "${paths[@]}"; do
    if [[ "$path" == *.aar ]]; then
        echo "Extracting JAR from $path"
        extract_jar_from_aar "$path"
    elif [[ "$path" == *.jar ]]; then
        jar_name=$(basename "$path")
        echo "Copying JAR from $path to $TARGET_DIR"
        cp -rf "$path" "$TARGET_DIR/$jar_name"
    fi
done

# Clean up
rm -rf "$TEMP_DIR"

echo "Classpath files have been copied to $TARGET_DIR"
