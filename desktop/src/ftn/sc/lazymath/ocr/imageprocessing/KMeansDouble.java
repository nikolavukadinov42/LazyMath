//
// Translated by CS2J (http://www.cs2j.com): 12/26/2014 18:22:55
//

package ftn.sc.lazymath.ocr.imageprocessing;


public class KMeansDouble   
{
//    public List<double> elementi = new List<double>();
//    public List<ClusterDouble> grupe = new List<ClusterDouble>();
//    public int brojGrupa = 0;
//    Random rnd = new Random();
//    public void podeliUGRupe(int brojGrupa, double errT, int tolerancija) throws Exception {
//        this.brojGrupa = brojGrupa;
//        if (brojGrupa == 0)
//            return ;
//
//        boolean flag = true;
//        while (flag)
//        {
//            grupe.Clear();
//            for (int i = 0;i < brojGrupa;i++)
//            {
//                //------------  inicijalizacijaa -------------
//                int ii = rnd.Next(elementi.Count);
//                ClusterDouble grupa = new ClusterDouble();
//                grupa.centar = elementi[ii];
//                grupe.Add(grupa);
//            }
//            for (int it = 0;it < 100;it++)
//            {
//                for (Object __dummyForeachVar0 : grupe)
//                {
//                    //------------- iterativno racunanje centara ---
//                    ClusterDouble grupa = (ClusterDouble)__dummyForeachVar0;
//                    grupa.elementi = new List<double>();
//                }
//                for (Object __dummyForeachVar1 : elementi)
//                {
//                    double cc = (Double)__dummyForeachVar1;
//                    int najblizaGrupa = 0;
//                    for (int i = 0;i < brojGrupa;i++)
//                    {
//                        if (grupe[najblizaGrupa].rastojanje(cc) > grupe[i].rastojanje(cc))
//                        {
//                            najblizaGrupa = i;
//                        }
//
//                    }
//                    grupe[najblizaGrupa].elementi.Add(cc);
//                }
//                double err = 0;
//                for (int i = 0;i < brojGrupa;i++)
//                {
//                    err += grupe[i].pomeriCentar();
//                }
//                if (err < errT)
//                    break;
//
//            }
//            flag = false;
//            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ g1 : grupe)
//            {
//                for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ g2 : grupe)
//                {
//                    if (g1.centar != g2.centar && Math.Abs(g1.centar - g2.centar) < tolerancija)
//                    {
//                        flag = true;
//                        break;
//                    }
//
//                }
//                if (flag)
//                    break;
//
//            }
//        }
//    }
//
//    public void podeliUGRupe(int brojGrupa, double errT) throws Exception {
//        this.brojGrupa = brojGrupa;
//        if (brojGrupa == 0)
//            return ;
//
//        for (int i = 0;i < brojGrupa;i++)
//        {
//            //------------  inicijalizacijaa -------------
//            int ii = rnd.Next(elementi.Count);
//            ClusterDouble grupa = new ClusterDouble();
//            grupa.centar = elementi[ii];
//            grupe.Add(grupa);
//        }
//        for (int it = 0;it < 100;it++)
//        {
//            for (Object __dummyForeachVar4 : grupe)
//            {
//                //------------- iterativno racunanje centara ---
//                ClusterDouble grupa = (ClusterDouble)__dummyForeachVar4;
//                grupa.elementi = new List<double>();
//            }
//            for (Object __dummyForeachVar5 : elementi)
//            {
//                double cc = (Double)__dummyForeachVar5;
//                int najblizaGrupa = 0;
//                for (int i = 0;i < brojGrupa;i++)
//                {
//                    if (grupe[najblizaGrupa].rastojanje(cc) > grupe[i].rastojanje(cc))
//                    {
//                        najblizaGrupa = i;
//                    }
//
//                }
//                grupe[najblizaGrupa].elementi.Add(cc);
//            }
//            double err = 0;
//            for (int i = 0;i < brojGrupa;i++)
//            {
//                err += grupe[i].pomeriCentar();
//            }
//            if (err < errT)
//                break;
//
//        }
//    }

}


