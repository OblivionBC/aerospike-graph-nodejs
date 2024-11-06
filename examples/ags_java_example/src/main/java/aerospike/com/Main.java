package aerospike.com;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

import java.util.Iterator;
import java.util.List;


public class Main {
    private static final String HOST = "172.18.0.3";
    private static final int PORT = 8182;
    private static final Cluster.Builder BUILDER = Cluster.build().addContactPoint(HOST).port(PORT).enableSsl(false);

    public static void main(String[] args) {
        final Cluster cluster = BUILDER.create();
        final GraphTraversalSource g = traversal().withRemote(DriverRemoteConnection.using(cluster));
        
        System.out.println("CONNECTED TO GRAPH, ADDING ELEMENTS");
        // Add 2 vertices and an edge between them with 2 properties each
        Vertex v1 = g.addV("V1")
            .property("vp1", "vpv1")
            .property("vp2", "vpv2")
            .next();

        Vertex v2 = g.addV("V2")
            .property("vp1", "vpv3")
            .property("vp2", "vpv4")
            .next();

        g.addE("connects").from(v1).to(v2)
            .property("ep1", "ev1")
            .property("ep2", "ev2")
            .iterate();

        System.out.println("READING BACK DATA..");
        Edge edge =  g.E().hasLabel("connects").next();

        Vertex inV = edge.inVertex();
        Vertex outV = edge.outVertex();
        System.out.println("Edge:");
        System.out.println(edge);
        System.out.println("Out from:");
        System.out.println(outV);
        System.out.println("In to:");
        System.out.println(inV);

        // List properties
        inV.properties().forEachRemaining(property -> {
            System.out.println(property.key() + " : " + property.value());
        });
        Iterator<VertexProperty<Object>> props = inV.properties();
        while(props.hasNext()){
            Property p = props.next();
            System.out.println("-->" + p.toString());
        }

        // Clean up
        g.V().drop().iterate();
        System.out.print("DONE, ");
        try {
            System.out.println("CLOSING CONNECT!");
            cluster.close();
        } catch (Exception e) {
            System.err.println("FAILED TO CLOSE!");
        }
    }
}