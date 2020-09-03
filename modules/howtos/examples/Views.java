import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.codec.TypeRef;
import com.couchbase.client.java.view.DesignDocumentNamespace;
import com.couchbase.client.java.view.ViewMetaData;
import com.couchbase.client.java.view.ViewOptions;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.couchbase.client.java.view.ViewScanConsistency;

import java.util.Optional;

import static com.couchbase.client.java.view.ViewOptions.viewOptions;

public class Views {

  public static void main(String... args) {

    {
      // #tag::views-simple[]
      Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
      Bucket bucket = cluster.bucket("bucket-name");

      ViewResult viewResult = bucket.viewQuery("design-doc-name", "view-name");
      for (ViewRow row : viewResult.rows()) {
        System.out.println("Found row: " + row);
      }
      // #end::views-simple[]
    }

    Cluster cluster = Cluster.connect("127.0.0.1", "username", "password");
    Bucket bucket = cluster.bucket("bucket-name");

    {
      // #tag::views-dev[]
      ViewResult viewResult = bucket.viewQuery(
        "ddoc",
        "view",
        viewOptions().namespace(DesignDocumentNamespace.DEVELOPMENT)
      );
      // #end::views-dev[]
    }

    {
      // #tag::views-opts[]
      ViewResult viewResult = bucket.viewQuery(
        "ddoc",
        "view",
        viewOptions()
          .scanConsistency(ViewScanConsistency.REQUEST_PLUS)
          .limit(5)
          .inclusiveEnd(true)
      );
      // #end::views-opts[]
    }

    {
      // #tag::views-meta[]
      ViewResult viewResult = bucket.viewQuery("ddoc", "view", viewOptions().debug(true));

      ViewMetaData viewMeta = viewResult.metaData();
      System.out.println("Got total rows: " + viewMeta.totalRows());
      viewMeta.debug().ifPresent(debug -> System.out.println("Got debug info as well: " + debug));
      // #end::views-meta[]
    }

  }

  // #tag::view_row_structure[]
  public class ViewRowExample {

    public Optional<String> id() { Optional<String> result = null; /*  */ return result;}

    public <T> Optional<T> keyAs(Class<T> target) { Optional<T>  result=null; /*  */ return result;}

    public <T> Optional<T> keyAs(TypeRef<T> target) { Optional<T>  result=null; /*  */ return result;}

    public <T> Optional<T> valueAs(Class<T> target) { Optional<T>  result=null; /*  */ return result;}

    public <T> Optional<T> valueAs(TypeRef<T> target) { Optional<T>  result=null; /*  */ return result;}
  }
  // #end::view_row_structure[]


}
