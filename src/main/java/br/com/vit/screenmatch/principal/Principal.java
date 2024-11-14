    package br.com.vit.screenmatch.principal;

    import br.com.vit.screenmatch.model.DadosEpisodios;
    import br.com.vit.screenmatch.model.DadosSerie;
    import br.com.vit.screenmatch.model.DadosTemporadas;
    import br.com.vit.screenmatch.model.Episodios;
    import br.com.vit.screenmatch.service.ConsumoApi;
    import br.com.vit.screenmatch.service.ConverteDados;

    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;
    import java.util.*;
    import java.util.stream.Collectors;

    public class Principal {

        private Scanner leitura = new Scanner(System.in);

        private ConsumoApi consumo = new ConsumoApi();
        private ConverteDados conversor = new ConverteDados();

        private final String ENDERECO = "http://www.omdbapi.com/?t=";
        private final String API_KEY = "&apikey=4acc54e1";

        public void exibeMenu(){
            System.out.println("Digite o nome da série para buscar: ");
            var nomeSerie = leitura.nextLine();
            var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
            DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
            System.out.println(dados);

            List<DadosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i<=dados.totalTemporadas(); i++){
                json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporadas dadosTemporadas = conversor.obterDados(json, DadosTemporadas.class);
                temporadas.add(dadosTemporadas);
            }
            temporadas.forEach(System.out::println);

            for (int i = 0; i < dados.totalTemporadas(); i++){
                List<DadosEpisodios> episodiosTemporada = temporadas.get(i).episodios();
                for (int j = 0; j < episodiosTemporada.size(); j++){
                    System.out.println(episodiosTemporada.get(j).titulo());
                }
            }

            temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

            List<String> nomes = Arrays.asList("Jaque", "Iasmin", "Paulo", "Rodrigo", "Nico");

            nomes.stream()
                    .sorted()
                    .limit(3)
                    .filter(n -> n.startsWith("N"))
                    .map(n -> n.toUpperCase())
                    .forEach(System.out::println);

            List<DadosEpisodios> dadosEpisodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream())
                    .collect(Collectors.toList());

            System.out.println("\nTop 10 episodios: ");
            dadosEpisodios.stream()
                    .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                    .peek(e -> System.out.println("Primeiro filtro(N/A): " + e))
                    .sorted(Comparator.comparing(DadosEpisodios::avaliacao).reversed())
                    .peek(e -> System.out.println("Ordenação; " + e))
                    .limit(10)
                    .peek(e -> System.out.println("Limite: " + e))
                    .map(e -> e.titulo().toUpperCase())
                    .peek(e -> System.out.println("Mapeamento: " + e))
                    .forEach(System.out::println);

            List<Episodios> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                            .map(d -> new Episodios(t.numero(), d))
                    ).collect(Collectors.toList());

            episodios.forEach(System.out::println);

            // Procurando um episódio por uma palavra chave
            System.out.println("Digite um trecho do título do episódio: ");
            var trechoDoTitulo = leitura.nextLine();
            Optional<Episodios> episodioBuscado = episodios.stream()
                    .filter(e -> e.getTitulo().toUpperCase().contains(trechoDoTitulo.toUpperCase()))
                    .findFirst();
            if (episodioBuscado.isPresent()){
                System.out.println("Episódio encontrado!");
                System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
            } else {
                System.out.println("Episódio não encontrado!");
            }


            System.out.println("A partir de que ano você deseja ver os episódios? ");
            var ano = leitura.nextLine();
            leitura.nextLine();

            LocalDate dataBusca = LocalDate.of(Integer.parseInt(ano), 1, 1);

            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            episodios.stream()
                    .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                    .forEach(e -> System.out.println(
                            "Temporada: " + e.getTemporada() +
                                    " Episodio: " + e.getTitulo() +
                                    " Data lançamento: " + e.getDataLancamento().format(formatador)
                    ));

            Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                    .filter(e -> e.getAvaliacao()>0.0)
                    .collect(Collectors.groupingBy(Episodios::getTemporada
                            , Collectors.averagingDouble(Episodios::getAvaliacao)));
            System.out.println(avaliacoesPorTemporada);

            //Pegando info sobre as avaliacoes da serie
            DoubleSummaryStatistics est = episodios.stream()
                    .filter(e -> e.getAvaliacao()>0.0)
                    .collect(Collectors.summarizingDouble(Episodios::getAvaliacao));
            System.out.println("Media: " + est.getAverage());
            System.out.println("Pior episódio: " + est.getMin());
            System.out.println("Melhor episódio: " + est.getMax());
            System.out.println("Quantidade de episodios avaliados : " + est.getCount());
        }
    }
