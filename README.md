Recentemente o design da empresa em que trabalho decidiu fazer uma reformulação em algumas telas do nosso app, visto que elas tinham contexto parecido, mas design totalmente diferente, tornando a experiência do usuário um pouco conflituosa.

Para montar o novo design desta telas, basicamente eu teria um coordinator Layout, com um card sobrepondo o coordinator, no qual o próprio conteúdo do card tinha a sua estrutura de widgets parecida, com algumas pequenas diferenças aqui e ali.

Como sabemos, infelizmente o framework do Android não nos permite de forma nativa ter uma estrutura de templates parecida com o desenvolvimento web, onde podemos definir partes estáticas e dinâmicas de um layout, para o reuso. Uma das formas de reutilização de layouts presente no framework é utilizar a tag `include`, que nos permite inserir um layout completo, mas não conseguimos modificar muito o conteúdo do mesmo, apenas atributos tipo `layout` do parent do layout sendo incluido.

Utilizar a tag `include` não seria útil no meu caso, visto que eu não conseguiria modificar apenas algumas partes do card, nem reutilizar todo o sistema de ter o coordinator com o card sobreposto, visto que o resto do conteúdo dos layouts não era totalmente igual.

Após assistir a excelente talk do Daniel Lew [Design Like a Coder: Efficient Android Layouts](https://news.realm.io/news/gotocph-daniel-lew-efficient-android-layouts/), eu aprendi sobre reutilização de layouts utilizando a tag `merge` e criando custom views.

Custom views permitem não só reutilizar layouts, como inserir lógica em nossas Views, sendo uma forma extremamente poderosa para alcançarmos nosso objetivo. Com elas, eu fui capaz de reutilizar layouts da forma que descrevi acima, muito parecido com reutilização de htmls no desenvolvimento web.

## Show me the code ##

Vamos mostrar um exemplo prático de como eu posso criar um Card reutilizável, na qual a estrutura da parte superior e inferior é estática, mas queremos que o que está entre seja dinâmico.

Inicialmente iremos criar o layout que agirá como template para nosso card.

`custom_card.xml`
```xml

<?xml version="1.0" encoding="utf-8"?>
<merge
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <android.support.constraint.ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:id="@+id/clCustomCardRoot"
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto">

        <TextView
            android:id="@+id/textView"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="This is the card title, it's common to all cards"
            android:layout_margin="16dp"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"/>

        <View
            android:id="@+id/placeholder"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_margin="8dp"
            app:layout_constraintTop_toBottomOf="@+id/textView"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"
            app:layout_constraintBottom_toTopOf="@+id/textView3"/>

        <TextView
            android:id="@+id/textView3"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="This is the card bottom, it's also common to all cards"
            android:layout_margin="16dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintLeft_toLeftOf="parent"
            app:layout_constraintRight_toRightOf="parent"/>
    </android.support.constraint.ConstraintLayout>

</merge>

```

Podemos observar que não temos um ViewGroup como parent neste layout. Estamos utilizando a tag `<merge>`, que nos permite [evitar redundância de layouts](https://developer.android.com/training/improving-layouts/reusing-layouts.html#Merge). Além disso, observe que utilizamos dois TextViews, um no topo e outro na parte inferior, para demonstrar a parte estática do nosso layout reutilizável. Para a parte dinâmica, observe que adicionamos uma View com id `placeholder`, com atributos de forma em que esta view fique entre os dois textos.

Após isso, iremos criar uma subclasse do `ViewGroup` que será o parent do nosso layout. Em nosso caso, queremos criar um card reutilizável.

`MyCustomCard.java`
```java

public class MyCustomCard extends CardView {

public MaintenanceCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        View rootView = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true);
}
```
Observe que ao inflar o layout, precisamos colocar o terceiro atributo `attachToRoot` como `true`, para que a nossa subclasse do CardView se torna o parent de todo o conteúdo que está entre a tag merge do layout sendo inflado.

Somente criando a subclasse e inflando o layout do nosso card ainda não temos o resultado desejado, ainda falta transformar nosso `placeholder` em um conteúdo dinâmico, para isso, iremos nos aproveitar de [atributos customizados](https://developer.android.com/training/custom-views/create-view.html#customattr) (custom attributes).

### Adicionando atributos customizados ###

Iremos adicionar um atributo chamado `cardType`, que nos permite escolher entre dois modelos de card: `cardA` e `cardB`, para isso, é necessário adicionar o arquivo `attrs` dentro de `res/vales`

`attrs.xml`

```xml

<?xml version="1.0" encoding="utf-8"?>
<resources>

    <declare-styleable name="MyCustomCard">
        <attr name="cardType" format="enum">
            <enum name="cardA" value="0"/>
            <enum name="cardB" value="1"/>
        </attr>
    </declare-styleable>

</resources>

```

Nosso atributo tem formato de `enum`, que contempla as duas opções mencionadas.

Após isso, iremos criar dois novos layouts, `card_a.xml` e `card_b.xml`, que terão o conteúdo dinâmico do nosso card:

`card_a.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="This is Card A dynamic content"
        android:textSize="18sp"
        android:textStyle="bold"
        android:textColor="@color/colorPrimary"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

</android.support.constraint.ConstraintLayout>


```

`card_b.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<android.support.constraint.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <Button
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="This is Card B dynamic content"
        android:textSize="16sp"
        android:textStyle="bold"
        android:textColor="@color/colorAccent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toTopOf="parent"/>

</android.support.constraint.ConstraintLayout>

```

Após isso, iremos modificar o `MyCustomCard.java` para que, baseado em nosso atributo, infle os layouts desejados no local do placeholder.


```java

public class MyCustomCard extends CardView {

    @Retention(SOURCE)
    @IntDef({
            TYPE_A,
            TYPE_B
    })
    public @interface CardType{}
    private transient static final int TYPE_A = 0;
    private transient static final int TYPE_B = 1;

    private Context mContext;
    private int mCardType;

    public MyCustomCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {

        mContext = context;
        View rootView = LayoutInflater.from(context).inflate(R.layout.custom_card, this, true);
        initAttrs(attrs);
        addMiddleContent();

    }

    private void initAttrs(AttributeSet attrs) {

        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.MyCustomCard,
                0, 0);

        try {
            mCardType = a.getInteger(R.styleable.MyCustomCard_cardType, 0);
        } finally {
            a.recycle();
        }

        //if we change some of the attributes dynamically with a method, we need to add this calls:
        //invalidate() and requestLayout() to that method;
        //more at: https://developer.android.com/training/custom-views/create-view.html#customattr

    }

    private void addMiddleContent() {

        ViewGroup root = (ViewGroup) findViewById(R.id.clCustomCardRoot);
        View placeholder = findViewById(R.id.placeholder);

        int layoutRes = retrieveMiddleLayoutRes(mCardType);

        View middleContent = LayoutInflater.from(mContext)
                .inflate(layoutRes, root, false);

        ViewGroup.LayoutParams params = placeholder.getLayoutParams();

        middleContent.setLayoutParams(params);

        root.removeView(placeholder);
        root.addView(middleContent);

    }

    @LayoutRes
    int retrieveMiddleLayoutRes(int cardType) {

        SparseIntArray layouts = new SparseIntArray();

        layouts.put(TYPE_A, R.layout.card_a);
        layouts.put(TYPE_B, R.layout.card_b);

        return layouts.get(cardType);
    }
```

Vamos analisar o que está acontecendo em cada método de nossa classe.

O método `initAttrs`, procura dentro dos atributos associados com o nosso card em um layout xml pelo nosso atributo `cardType` e armazena no nosso atributo `mCardType`
 
O método `addMiddleContent` é o responsável pela mágica de colocar o layout correto no local do placeholder. Para isso, inicialmente procuramos pelo ViewGroup que é o pai do nosso placeholder (Como estamos utilizando um CardView, geralmente precisamos de um outro ViewGroup como filho para ser responsável pelo alinhamento das views, que neste caso é um constraint Layout) e o próprio placeholder. Após isso, utilzando o nosso atributo de card, utilizamos o método `retrieveMiddleLayoutRes` para procurar em um Map o layout desejado. (A criação deste mapa deixa mais claro e limpo do que um `if-else` ou `switch-case`)

Após isso, procuramos pelo próprio placeholder e armazenamos o seu `LayoutParams` em uma variável. Neste ponto, pode parecer desnecessário termos uma view para ficar de placeholder somente para facilitar o alinhamento, visto que poderíamos criar o LayoutParams manualmente, porém, foi utilizada essa solução para que se por algum motivo quisermos trocar o ConstraintLayout para outro elemento, não ser necessário ter que ir na minha custom view e modificar a criação destes parâmetros. Dito isto, inflamos o layout correto, configuramos seu LayoutParams para ser igual ao do placeholder e realizamos a troca das Views.

Com isso, temos nosso card, no qual é possível dinamicamente modificarmos somente uma parte dele, tornando-o altamente reutilizável.

### Utilizando nosso card ###

Para utilizar o card, basta adicionarmos nosso card em um layout qualquer, como qualquer outro card, e configurar nosso atributo `cardType` para uma das duas opções.


`activity_main.xml`


```xml

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <com.igorvd.layout_templates.MyCustomCard
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardElevation="4dp"
        app:cardType="cardA"
        />

    <com.igorvd.layout_templates.MyCustomCard
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        app:cardElevation="4dp"
        app:cardType="cardB"
        />

</LinearLayout>

```
![example](https://github.com/igorvilela28/Layout-Templates-Sample/blob/master/wiki/screenshots/layout-template-sample.png?raw=true)


Ah, e o mais legal de tudo, quando você troca o cardType, você consegue ver as modificações ocorrerem no editor de Layout, não é necessário ter que instalar o app para ver somente em tempo de execução. =)

Você pode encontrar o sample com o exemplo acima no GitHub.
